/*
 * Copyright 2015 LoiLo inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.loilo.promise;

import android.util.Log;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//Javaの仕様でSleepやLock等のブロック処理を抜ける仕組みがThread.interruptしかない。
// Threadの割り込み処理によるキャンセル処理をうまくフレームワーク化したいので作っているライブラリになります。
@SuppressWarnings("TryFinallyCanBeTryWithResources")
public final class Promises {
    private static final ExecutorService mDefaultExecutorService = Executors.newCachedThreadPool();

    public static <TOut> Promise<TOut> when(WhenCallback<TOut> whenCallback) {
        return new InitialPromise<>(whenCallback);
    }

    public static <TOut> Promise<TOut> success(final TOut out) {
        return when(new WhenCallback<TOut>() {

            @Override
            public Deferred<TOut> run(final WhenParams params) throws Exception {
                return Defer.success(out);
            }
        });
    }

    public static <TOut> Promise<TOut> fail(final Exception e) {
        return when(new WhenCallback<TOut>() {

            @Override
            public Deferred<TOut> run(WhenParams params) throws Exception {
                return Defer.fail(e);
            }
        });
    }

    public static <TOut> Promise<TOut> cancel() {
        return when(new WhenCallback<TOut>() {

            @Override
            public Deferred<TOut> run(WhenParams params) throws Exception {
                return Defer.cancel();
            }
        });
    }

    public static <TOut> Promise<TOut> notImpl() {
        return when(new WhenCallback<TOut>() {

            @Override
            public Deferred<TOut> run(WhenParams params) throws Exception {
                return Defer.notImpl();
            }
        });
    }

    public static <TOut> Repeat<TOut> repeat(final RepeatCallback<TOut> callback) {
        return new Repeat<TOut>() {

            @Override
            public Promise<TOut> until(final UntilCallback<TOut> untilCallback) {
                return when(new WhenCallback<TOut>() {

                    @Override
                    public Deferred<TOut> run(final WhenParams params) throws Exception {

                        final Detachable<ArrayCloseableStack> scope = new Detachable<>();
                        try {
                            Result<TOut> result;
                            do {
                                {
                                    final ArrayCloseableStack closing = scope.detach();
                                    if (closing != null) {
                                        closing.close();
                                    }
                                }

                                scope.attach(new ArrayCloseableStack());

                                Deferred<TOut> deferred;
                                if (params.getCancelToken().isCanceled()) {
                                    deferred = Defer.cancel();
                                } else {
                                    try {
                                        deferred = callback.run(new RepeatParams(params.getCancelToken(), scope.ref(), params.getTag()));
                                    } catch (final InterruptedException e) {
                                        deferred = Defer.cancel();
                                        Thread.currentThread().interrupt();
                                    } catch (final CancellationException e) {
                                        deferred = Defer.cancel();
                                    } catch (final Exception e) {
                                        deferred = Defer.fail(e);
                                    }
                                }

                                result = Results.exchangeCancelToken(deferred.getResult(), params.getCancelToken());
                            }
                            while (!result.getCancelToken().isCanceled()
                                    && !untilCallback.run(new UntilParams<>(result, scope.ref(), params.getTag())));

                            params.getScope().push(scope.detach());

                            return Defer.complete(result);
                        } finally {
                            scope.close();
                        }
                    }
                });
            }
        };
    }

    public static <TIn> Promise<Void> forEach(final Iterable<TIn> ite, final ForEachCallback<TIn> callback) {
        return when(new WhenCallback<Void>() {

            @Override
            public Deferred<Void> run(WhenParams args) throws Exception {
                for (TIn element : ite) {
                    if (args.getCancelToken().isCanceled()) {
                        return Defer.cancel();
                    }

                    final ArrayCloseableStack scope = new ArrayCloseableStack();
                    try {
                        final Deferred<ForEachOp> result = callback.run(new ForEachParams<>(element, args.getCancelToken(), scope, args.getTag()));
                        final ForEachOp next = result.getResult().safeGetValue();
                        if (next == ForEachOp.BREAK) {
                            return Defer.success(null);
                        }
                    } finally {
                        scope.close();
                    }
                }

                return Defer.success(null);
            }
        });
    }

    private static <TIn, TOut> Deferred<TOut> runSuccessCallback(final ResultParams<TIn> params, final SuccessCallback<TIn, TOut> callback) throws Exception {

        final CancelToken cancelToken = params.getCancelToken();
        if (cancelToken.isCanceled()) {
            return Defer.cancel();
        }

        final Exception e = params.getException();
        if (e != null) {
            return Defer.fail(e);
        }

        return callback.run(new SuccessParams<>(
                params.getValue(), cancelToken, params.getScope(), params.getTag()));
    }

    private static <TIn> Deferred<TIn> runFailCallback(final ResultParams<TIn> params, final FailCallback<TIn> callback) throws Exception {

        final CancelToken cancelToken = params.getCancelToken();
        if (cancelToken.isCanceled()) {
            return Defer.cancel();
        }

        final Exception e = params.getException();
        if (e != null) {
            return callback.run(new FailParams<TIn>(e, cancelToken, params.getScope(), params.getTag()));
        }

        return Defer.success(params.getValue());
    }


    private interface EntryPoint extends Submittable {
        void execute(final CancelToken cancelToken, final CloseableStack scope, final Object tag);
    }

    private interface NextPoint<TIn> {
        void execute(final Result<TIn> input, final CancelToken cancelToken, final CloseableStack scope, final Object tag);
    }

    private static final class FutureCanceller implements Canceller {

        private final Lock mLock;
        private final Runnable mCancelCallback;
        private volatile boolean mIsLaunched;
        private volatile boolean mIsCanceled;
        private volatile Future<?> mFuture;

        public FutureCanceller(final Runnable cancelCallback) {
            mCancelCallback = cancelCallback;
            mLock = new ReentrantLock();
        }

        @Override
        public void cancel() {
            mLock.lock();
            try {
                if (mIsCanceled) {
                    return;
                }

                mIsCanceled = true;
                if (mFuture != null) {
                    mFuture.cancel(true);
                }
                if (mIsLaunched) {
                    return;
                }

            } finally {
                mLock.unlock();
            }

            //実行前にキャンセルしたら、デフォルトのスレッドからキャンセルコールバックする。
            mDefaultExecutorService.execute(mCancelCallback);
        }

        @Override
        public boolean isCanceled() {
            mLock.lock();
            try {
                return mIsCanceled;
            } finally {
                mLock.unlock();
            }
        }

        public boolean notifyLaunched() {
            mLock.lock();
            try {
                mIsLaunched = true;
                return !mIsCanceled;
            } finally {
                mLock.unlock();
            }
        }

        public void setFuture(final Future<?> future) {
            mLock.lock();
            try {
                mFuture = future;
            } finally {
                mLock.unlock();
            }
        }
    }

    private static final class NextDeferred<TOut> implements NextPoint<TOut>, Deferred<TOut> {

        private final Deferrable<TOut> mDeferrable;

        public NextDeferred() {
            mDeferrable = new Deferrable<>();
        }

        @Override
        public void execute(final Result<TOut> input, final CancelToken cancelToken, final CloseableStack scope, final Object tag) {
            mDeferrable.setResult(input);
        }


        @Override
        public Result<TOut> getResult() {
            return mDeferrable.getResult();
        }
    }

    private static final class LastPromise<TIn> implements Submittable, NextPoint<TIn> {

        private final EntryPoint mEntryPoint;
        private final FinishCallback<TIn> mFinishCallback;

        public LastPromise(final EntryPoint entryPoint, final FinishCallback<TIn> finishCallback) {
            mEntryPoint = entryPoint;
            mFinishCallback = finishCallback;
        }


        @Override
        public Canceller submitOn(final ExecutorService executorService, final Object tag) {
            return mEntryPoint.submitOn(executorService, tag);
        }


        @Override
        public Canceller submitOn(ExecutorService executorService) {
            return submitOn(executorService, null);
        }


        @Override
        public Canceller submit(Object tag) {
            return submitOn(mDefaultExecutorService, tag);
        }


        @Override
        public Canceller submit() {
            return submitOn(mDefaultExecutorService);
        }

        @Override
        public void execute(final Result<TIn> input, final CancelToken cancelToken, final CloseableStack scope, final Object tag) {
            mFinishCallback.run(new FinishParams<>(input, scope, tag));
        }
    }

    private static final class ContinuationPromise<TIn, TOut> implements Promise<TOut>, NextPoint<TIn> {

        private final EntryPoint mEntryPoint;
        private final ThenCallback<TIn, TOut> mThenCallback;
        private NextPoint<TOut> mNextPoint;

        public ContinuationPromise(final EntryPoint entryPoint, final ThenCallback<TIn, TOut> thenCallback) {
            mEntryPoint = entryPoint;
            mThenCallback = thenCallback;
        }

        public void setNextPoint(NextPoint<TOut> nextPoint) {
            mNextPoint = nextPoint;
        }

        @Override
        public void execute(final Result<TIn> input, final CancelToken cancelToken, final CloseableStack scope, final Object tag) {
            Deferred<TOut> deferred;
            //ここはキャンセルしてても必ずコールバックを呼ぶようにしています。
            try {
                deferred = mThenCallback.run(new ThenParams<>(input, scope, tag));
            } catch (final InterruptedException e) {
                deferred = Defer.cancel();
                Thread.currentThread().interrupt();
            } catch (final CancellationException e) {
                deferred = Defer.cancel();
            } catch (final Exception e) {
                deferred = Defer.fail(e);
            }

            final Result<TOut> output = Results.exchangeCancelToken(deferred.getResult(), cancelToken);

            if (mNextPoint != null) {
                mNextPoint.execute(output, cancelToken, scope, tag);
            }
        }


        @Override
        public Deferred<TOut> get(TaggedCancelState state) {
            final NextDeferred<TOut> next = new NextDeferred<>();
            setNextPoint(next);

            final ArrayCloseableStack scope = new ArrayCloseableStack();
            try {
                mEntryPoint.execute(state.getCancelToken(), scope, state.getTag());
                return next;
            } finally {
                scope.close();
            }
        }


        @Override
        public Deferred<TOut> getOn(ExecutorService executorService, Tagged state) {
            final Deferrable<TOut> deferred = new Deferrable<>();

            final Canceller canceller = finish(new FinishCallback<TOut>() {
                @Override
                public void run(final FinishParams<TOut> params) {
                    deferred.setResult(params.asResult());
                }
            }).submitOn(executorService, state.getTag());

            deferred.setCancellable(canceller);

            return deferred;
        }


        @Override
        public Promise<TOut> promiseOn(final ExecutorService executorService) {
            return when(new WhenCallback<TOut>() {

                @Override
                public Deferred<TOut> run(WhenParams params) throws Exception {
                    return getOn(executorService, params);
                }
            });
        }


        @Override
        public Canceller submitOn(final ExecutorService executorService, final Object tag) {
            return mEntryPoint.submitOn(executorService, tag);
        }


        @Override
        public Canceller submitOn(ExecutorService executorService) {
            return submitOn(executorService, null);
        }


        @Override
        public Canceller submit(Object tag) {
            return submitOn(mDefaultExecutorService, tag);
        }


        @Override
        public Canceller submit() {
            return submitOn(mDefaultExecutorService);
        }


        @Override
        public <TNextOut> Promise<TNextOut> then(ThenCallback<TOut, TNextOut> thenCallback) {
            final ContinuationPromise<TOut, TNextOut> continuationTask = new ContinuationPromise<>(mEntryPoint, thenCallback);
            setNextPoint(continuationTask);
            return continuationTask;
        }


        @Override
        public Promise<TOut> watch(final WatchCallback<TOut> watchCallback) {
            final ContinuationPromise<TOut, TOut> continuationTask = new ContinuationPromise<>(mEntryPoint, new ThenCallback<TOut, TOut>() {

                @Override
                public Deferred<TOut> run(ThenParams<TOut> params) throws Exception {
                    watchCallback.run(params);
                    return params.asDeferred();
                }
            });
            setNextPoint(continuationTask);
            return continuationTask;
        }


        @Override
        public <TNextOut> Promise<TNextOut> succeeded(final SuccessCallback<TOut, TNextOut> successCallback) {
            final ContinuationPromise<TOut, TNextOut> continuationTask = new ContinuationPromise<>(mEntryPoint, new ThenCallback<TOut, TNextOut>() {

                @Override
                public Deferred<TNextOut> run(final ThenParams<TOut> params) throws Exception {
                    return runSuccessCallback(params, successCallback);
                }
            });
            setNextPoint(continuationTask);
            return continuationTask;
        }


        @Override
        public Promise<TOut> failed(final FailCallback<TOut> failCallback) {
            final ContinuationPromise<TOut, TOut> continuationTask = new ContinuationPromise<>(mEntryPoint, new ThenCallback<TOut, TOut>() {

                @Override
                public Deferred<TOut> run(final ThenParams<TOut> params) throws Exception {
                    return runFailCallback(params, failCallback);
                }
            });
            setNextPoint(continuationTask);
            return continuationTask;
        }


        @Override
        public Submittable finish(FinishCallback<TOut> finishCallback) {
            final LastPromise<TOut> last = new LastPromise<>(mEntryPoint, finishCallback);
            setNextPoint(last);
            return last;
        }


        @Override
        public <TReplace> Promise<TReplace> exchange(final TReplace replace) {
            return then(new ThenCallback<TOut, TReplace>() {

                @Override
                public Deferred<TReplace> run(ThenParams<TOut> params) throws Exception {
                    return Defer.exchangeValue(params.asResult(), replace);
                }
            });
        }
    }

    private static final class InitialPromise<TOut> implements Promise<TOut>, EntryPoint {

        private final WhenCallback<TOut> mWhenCallback;
        private NextPoint<TOut> mNextPoint;

        public InitialPromise(WhenCallback<TOut> whenCallback) {
            mWhenCallback = whenCallback;
        }

        public void setNextPoint(NextPoint<TOut> nextPoint) {
            mNextPoint = nextPoint;
        }

        @Override
        public void execute(final CancelToken cancelToken, final CloseableStack scope, final Object tag) {

            Deferred<TOut> handle;
            //キャンセルされていたら最初のFunctionは呼び出しません。
            if (cancelToken.isCanceled()) {
                handle = Defer.cancel();
            } else {
                try {
                    handle = mWhenCallback.run(new WhenParams(cancelToken, scope, tag));
                } catch (final InterruptedException e) {
                    handle = Defer.cancel();
                    Thread.currentThread().interrupt();
                } catch (final CancellationException e) {
                    handle = Defer.cancel();
                } catch (final Exception e) {
                    handle = Defer.fail(e);
                }
            }

            final Result<TOut> result = Results.exchangeCancelToken(handle.getResult(), cancelToken);

            if (mNextPoint != null) {
                mNextPoint.execute(result, cancelToken, scope, tag);
            }
        }


        @Override
        public Deferred<TOut> get(TaggedCancelState state) {
            final NextDeferred<TOut> next = new NextDeferred<>();
            setNextPoint(next);

            final ArrayCloseableStack scope = new ArrayCloseableStack();
            try {
                execute(state.getCancelToken(), scope, state.getTag());
                return next;
            } finally {
                scope.close();
            }
        }


        @Override
        public Deferred<TOut> getOn(ExecutorService executorService, Tagged state) {

            final Deferrable<TOut> deferred = new Deferrable<>();

            final Canceller canceller = finish(new FinishCallback<TOut>() {
                @Override
                public void run(final FinishParams<TOut> args) {
                    deferred.setResult(args.asResult());
                }
            }).submitOn(executorService, state.getTag());

            deferred.setCancellable(canceller);

            return deferred;
        }


        @Override
        public Promise<TOut> promiseOn(final ExecutorService executorService) {
            return when(new WhenCallback<TOut>() {

                @Override
                public Deferred<TOut> run(final WhenParams params) throws Exception {
                    return getOn(executorService, params);
                }
            });
        }


        @Override
        public Canceller submitOn(final ExecutorService executorService, final Object tag) {
            //ここがPromiseのエントリーポイントになります。他のPromiseのインスタンスも結局これを呼び出しています。
            final FutureCanceller canceller = new FutureCanceller(new Runnable() {
                @Override
                public void run() {
                    boolean hasCriticalError = false;
                    final ArrayCloseableStack scope = new ArrayCloseableStack();
                    try {
                        execute(CancelTokens.CANCELED, scope, tag);
                    } catch (final Exception e) {
                        hasCriticalError = true;
                        Log.e("loilo-promise", "InitialPromise: Promise exception occurred on canceling.", e);
                        Dispatcher.getMainDispatcher().run(new Runnable() {
                            @Override
                            public void run() {
                                //メインスレッドで例外を飛ばす。これでアプリを終了させる。
                                throw e;
                            }
                        });
                    } catch (final Error e) {
                        hasCriticalError = true;
                        Log.wtf("loilo-promise", "InitialPromise: Promise error occurred on canceling.", e);
                        Dispatcher.getMainDispatcher().run(new Runnable() {
                            @Override
                            public void run() {
                                //メインスレッドで例外を飛ばす。これでアプリを終了させる。
                                throw e;
                            }
                        });
                    } finally {
                        try {
                            scope.close();
                        } catch (final Error e) {

                            if (!hasCriticalError) {
                                Log.wtf("loilo-promise", "InitialPromise: Scope close error occurred on canceling.", e);
                                Dispatcher.getMainDispatcher().run(new Runnable() {
                                    @Override
                                    public void run() {
                                        //メインスレッドで例外を飛ばす。これでアプリを終了させる。
                                        throw e;
                                    }
                                });
                            }
                        }
                    }
                }
            });
            final Future<?> future = executorService.submit(new Runnable() {
                @Override
                public void run() {

                    if (!canceller.notifyLaunched()) {
                        return;
                    }

                    boolean hasCriticalError = false;
                    final ArrayCloseableStack scope = new ArrayCloseableStack();
                    try {
                        execute(canceller, scope, tag);
                    } catch (final Exception e) {
                        hasCriticalError = true;
                        Log.e("loilo-promise", "InitialPromise: Promise exception occurred.", e);
                        Dispatcher.getMainDispatcher().run(new Runnable() {
                            @Override
                            public void run() {
                                //メインスレッドで例外を飛ばす。これでアプリを終了させる。
                                throw e;
                            }
                        });
                    } catch (final Error e) {
                        hasCriticalError = true;
                        Log.wtf("loilo-promise", "InitialPromise: Promise error occurred.", e);
                        Dispatcher.getMainDispatcher().run(new Runnable() {
                            @Override
                            public void run() {
                                //メインスレッドで例外を飛ばす。これでアプリを終了させる。
                                throw e;
                            }
                        });
                    } finally {
                        try {
                            scope.close();
                        } catch (final Error e) {

                            if (!hasCriticalError) {
                                Log.wtf("loilo-promise", "InitialPromise: Scope close error occurred.", e);
                                Dispatcher.getMainDispatcher().run(new Runnable() {
                                    @Override
                                    public void run() {
                                        //メインスレッドで例外を飛ばす。これでアプリを終了させる。
                                        throw e;
                                    }
                                });
                            }
                        }
                    }
                }
            });
            canceller.setFuture(future);
            return canceller;
        }


        @Override
        public Canceller submitOn(ExecutorService executorService) {
            return submitOn(executorService, null);
        }


        @Override
        public Canceller submit(Object tag) {
            return submitOn(mDefaultExecutorService, tag);
        }


        @Override
        public Canceller submit() {
            return submitOn(mDefaultExecutorService);
        }


        @Override
        public <TNextOut> Promise<TNextOut> then(final ThenCallback<TOut, TNextOut> thenCallback) {
            final ContinuationPromise<TOut, TNextOut> continuationTask = new ContinuationPromise<>(this, thenCallback);
            setNextPoint(continuationTask);
            return continuationTask;
        }


        @Override
        public Promise<TOut> watch(final WatchCallback<TOut> watchCallback) {
            final ContinuationPromise<TOut, TOut> continuationTask = new ContinuationPromise<>(this, new ThenCallback<TOut, TOut>() {

                @Override
                public Deferred<TOut> run(ThenParams<TOut> params) throws Exception {
                    watchCallback.run(params);
                    return params.asDeferred();
                }
            });
            setNextPoint(continuationTask);
            return continuationTask;
        }


        @Override
        public <TNextOut> Promise<TNextOut> succeeded(final SuccessCallback<TOut, TNextOut> successCallback) {
            final ContinuationPromise<TOut, TNextOut> continuationTask = new ContinuationPromise<>(this, new ThenCallback<TOut, TNextOut>() {

                @Override
                public Deferred<TNextOut> run(final ThenParams<TOut> params) throws Exception {
                    return runSuccessCallback(params, successCallback);
                }
            });
            setNextPoint(continuationTask);
            return continuationTask;
        }


        @Override
        public Promise<TOut> failed(final FailCallback<TOut> failCallback) {
            final ContinuationPromise<TOut, TOut> continuationTask = new ContinuationPromise<>(this, new ThenCallback<TOut, TOut>() {

                @Override
                public Deferred<TOut> run(final ThenParams<TOut> params) throws Exception {
                    return runFailCallback(params, failCallback);
                }
            });
            setNextPoint(continuationTask);
            return continuationTask;
        }


        @Override
        public Submittable finish(final FinishCallback<TOut> finishCallback) {
            final LastPromise<TOut> last = new LastPromise<>(this, finishCallback);
            setNextPoint(last);
            return last;
        }


        @Override
        public <TReplace> Promise<TReplace> exchange(final TReplace replace) {
            return then(new ThenCallback<TOut, TReplace>() {

                @Override
                public Deferred<TReplace> run(final ThenParams<TOut> params) throws Exception {
                    return Defer.exchangeValue(params.asResult(), replace);
                }
            });
        }
    }
}