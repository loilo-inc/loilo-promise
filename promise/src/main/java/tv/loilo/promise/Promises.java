/*
 * Copyright (c) 2015-2016 LoiLo inc.
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is {@link Promise} Factory.
 */
@SuppressWarnings("TryFinallyCanBeTryWithResources")
public final class Promises {
    private static final ExecutorService mDefaultExecutorService = Executors.newCachedThreadPool();

    private Promises() {
    }

    @SafeVarargs
    public static <TOut> Promise<Mashup<TOut>> whenAll(final Mashup<TOut> mashup, final Promise<TOut>... promises) {
        return when(new WhenCallback<Mashup<TOut>>() {
            @Override
            public Deferred<Mashup<TOut>> run(WhenParams entryParams) throws Exception {

                final int promiseCount = promises.length;
                final AtomicInteger finishCount = new AtomicInteger();
                final AtomicReference<Exception> exception = new AtomicReference<>();
                final Scheduler scheduler = new Scheduler(1);

                final Deferrable<Mashup<TOut>> deferrable = new Deferrable<>();
                final ManualResetEvent prepared = new ManualResetEvent(false);
                final Canceller[] cancellers = new Canceller[promiseCount];
                final AtomicInteger successCount = new AtomicInteger();

                try{
                    for (int i = 0; i < promiseCount; ++i) {
                        final Promise<TOut> promise = promises[i];
                        final int capture = i;
                        final Canceller canceller = Promises.when(new WhenCallback<TOut>() {
                            @Override
                            public Deferred<TOut> run(WhenParams params) throws Exception {
                                prepared.await();
                                return promise.get(params);
                            }
                        }).then(new ThenCallback<TOut, Void>() {
                            @Override
                            public Deferred<Void> run(final ThenParams<TOut> eachParams) throws Exception {
                                return when(new WhenCallback<Void>() {
                                    @Override
                                    public Deferred<Void> run(WhenParams unused) throws Exception {
                                        if (eachParams.getCancelToken().isCanceled()) {
                                            return Defer.cancel();
                                        }

                                        final Exception e = eachParams.getException();
                                        if (e != null) {
                                            return Defer.fail(e);
                                        }

                                        if(mashup != null){
                                            mashup.add(capture, eachParams.getValue());
                                        }
                                        successCount.incrementAndGet();
                                        return Defer.success(null);
                                    }
                                }).getOn(scheduler, eachParams);
                            }
                        }).finish(new FinishCallback<Void>() {
                            @Override
                            public void run(FinishParams<Void> finishParams) {
                                if (!finishParams.getCancelToken().isCanceled()) {
                                    final Exception e = finishParams.getException();
                                    if(e != null){
                                        if (exception.compareAndSet(null, e)) {
                                            for (int j = 0; j < promiseCount; ++j) {
                                                if (j == capture) {
                                                    continue;
                                                }
                                                final Canceller canceling = cancellers[j];
                                                if(canceling != null){
                                                    canceling.cancel();
                                                }
                                            }
                                        }
                                    }
                                }

                                if (finishCount.incrementAndGet() >= promiseCount) {
                                    final Exception e = exception.get();
                                    if (e != null) {
                                        deferrable.setFailed(e);
                                    } else {
                                        if(successCount.get() >= promiseCount){
                                            deferrable.setSucceeded(mashup);
                                        } else {
                                            deferrable.setCanceled();
                                        }
                                    }
                                }
                            }
                        }).submit(entryParams.getTag());
                        cancellers[i] = canceller;
                    }
                } finally {
                    prepared.set();
                }

                deferrable.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() {
                        for (Canceller canceller : cancellers) {
                            canceller.cancel();
                        }
                    }
                });

                return deferrable;
            }
        });
    }

    @SafeVarargs
    public static <TOut> Promise<TOut> whenAny(final Promise<TOut>... promises) {
        return when(new WhenCallback<TOut>() {
            @Override
            public Deferred<TOut> run(WhenParams entryParams) throws Exception {

                final int promiseCount = promises.length;
                final AtomicInteger finishCount = new AtomicInteger();
                final AtomicReference<Result<TOut>> result = new AtomicReference<>();

                final Deferrable<TOut> deferrable = new Deferrable<>();
                final ManualResetEvent prepared = new ManualResetEvent(false);
                final Canceller[] cancellers = new Canceller[promiseCount];

                try {
                    for (int i = 0; i < promiseCount; ++i) {
                        final Promise<TOut> promise = promises[i];
                        final int capture = i;
                        final Canceller canceller = Promises.when(new WhenCallback<TOut>() {
                            @Override
                            public Deferred<TOut> run(WhenParams params) throws Exception {
                                prepared.await();
                                return promise.get(params);
                            }
                        }).finish(new FinishCallback<TOut>() {
                            @Override
                            public void run(FinishParams<TOut> finishParams) {

                                if(!finishParams.getCancelToken().isCanceled()){
                                    if (result.compareAndSet(null, finishParams.asResult())) {
                                        for (int j = 0; j < promiseCount; ++j) {
                                            if (j == capture) {
                                                continue;
                                            }
                                            final Canceller canceling = cancellers[j];
                                            if(canceling != null){
                                                canceling.cancel();
                                            }
                                        }
                                    }
                                }

                                if (finishCount.incrementAndGet() >= promiseCount) {
                                    final Result<TOut> ret = result.get();
                                    if(ret != null){
                                        deferrable.setResult(ret);
                                    } else {
                                        deferrable.setCanceled();
                                    }
                                }
                            }
                        }).submit(entryParams.getTag());
                        cancellers[i] = canceller;
                    }
                } finally {
                    prepared.set();
                }

                deferrable.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() {
                        for (Canceller canceller : cancellers) {
                            canceller.cancel();
                        }
                    }
                });

                return deferrable;
            }
        });
    }

    /**
     * Promise to return a result of the callback.
     * The callback will be execute asynchronously on background thread.
     * Promise will be executing when you called a method to submit.
     *
     * If Promise is canceled before the callback execution,
     * this callback call is skipped, and calls the subsequent callback.
     *
     * @param whenCallback the callback when Promise submitted
     * @param <TOut>       the type of the callback result value
     * @return promise to return a result
     */
    public static <TOut> Promise<TOut> when(WhenCallback<TOut> whenCallback) {
        return new InitialPromise<>(whenCallback);
    }

    /**
     * Promise to return a value.
     *
     * @param out    the output value
     * @param <TOut> the type of the output value
     * @return promise to return a output value
     */
    public static <TOut> Promise<TOut> success(final TOut out) {
        return when(new WhenCallback<TOut>() {

            @Override
            public Deferred<TOut> run(final WhenParams params) throws Exception {
                return Defer.success(out);
            }
        });
    }

    /**
     * Promise to fail by exception.
     *
     * @param e      the exception of failure cause
     * @param <TOut> the type of the output value if Promise were to success
     * @return promise to return a failure result
     */
    public static <TOut> Promise<TOut> fail(final Exception e) {
        return when(new WhenCallback<TOut>() {

            @Override
            public Deferred<TOut> run(WhenParams params) throws Exception {
                return Defer.fail(e);
            }
        });
    }

    /**
     * Promise to cancel.
     *
     * @param <TOut> the type of the output value if Promise were to success
     * @return promise to return a canceled result
     */
    public static <TOut> Promise<TOut> cancel() {
        return when(new WhenCallback<TOut>() {

            @Override
            public Deferred<TOut> run(WhenParams params) throws Exception {
                return Defer.cancel();
            }
        });
    }

    /**
     * Promise to fail by exception that means not implemented.
     *
     * @param <TOut> the type of the output value if Promise were to success
     * @return promise to return a failure result
     */
    public static <TOut> Promise<TOut> notImpl() {
        return when(new WhenCallback<TOut>() {

            @Override
            public Deferred<TOut> run(WhenParams params) throws Exception {
                return Defer.notImpl();
            }
        });
    }

    /**
     * Promise to repeat executing the callback.
     *
     * @param callback the callback to repeat
     * @param <TOut>   the type of the callback result value
     * @return the object that sets repeated conditions
     */
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
                            final AtomicInteger index = new AtomicInteger();
                            do {
                                {
                                    final ArrayCloseableStack closing = scope.detach();
                                    if (closing != null) {
                                        closing.close();
                                    }
                                }

                                scope.attach(new ArrayCloseableStack());

                                Deferred<TOut> deferred = null;
                                if (params.getCancelToken().isCanceled()) {
                                    deferred = Defer.cancel();
                                } else {
                                    try {
                                        deferred = callback.run(new RepeatParams(index, params.getCancelToken(), scope.ref(), params.getTag()));
                                    } catch (final InterruptedException e) {
                                        deferred = Defer.cancel();
                                        Thread.currentThread().interrupt();
                                    } catch (final CancellationException e) {
                                        deferred = Defer.cancel();
                                    } catch (final Exception e) {
                                        deferred = Defer.fail(e);
                                    } finally {
                                        if (deferred == null) {
                                            deferred = Defer.fail(new NullDeferredException("RepeatCallback returned null"));
                                        }
                                    }
                                }

                                result = Results.exchangeCancelToken(deferred.getResult(), params.getCancelToken());
                            }
                            while (!result.getCancelToken().isCanceled()
                                    && !untilCallback.run(new UntilParams<>(index, result, scope.ref(), params.getTag())));

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

    /**
     * Promise to enumerate the members of the collection and to perform the given callback on each element.
     *
     * @param ite      the collection of enumeration target
     * @param operand  the value that will be passed to the callback and will be a result of promise
     * @param callback the callback on each element.
     * @param <TIn>    the type of element
     * @param <TOut>   the type of operand
     * @return promise to return the operand value
     */
    public static <TIn, TOut> Promise<TOut> forEach(final Iterable<TIn> ite, final TOut operand, final ForEachCallback<TIn, TOut> callback) {
        return when(new WhenCallback<TOut>() {

            @Override
            public Deferred<TOut> run(WhenParams args) throws Exception {
                final AtomicInteger index = new AtomicInteger();
                for (TIn element : ite) {
                    if (args.getCancelToken().isCanceled()) {
                        return Defer.cancel();
                    }

                    final ArrayCloseableStack scope = new ArrayCloseableStack();
                    try {
                        final Deferred<ForEachOp> result = callback.run(new ForEachParams<>(index, element, operand, args.getCancelToken(), scope, args.getTag()));
                        final ForEachOp next = result.getResult().safeGetValue();
                        if (next == ForEachOp.BREAK) {
                            return Defer.success(operand);
                        }
                    } finally {
                        scope.close();
                    }
                }

                return Defer.success(operand);
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

            //If Promise was canceled before the execution, invoke the callback on the default thread.
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
        public Canceller submitOn(Scheduler scheduler, Object tag) {
            return mEntryPoint.submitOn(scheduler, tag);
        }

        @Override
        public Canceller submitOn(Scheduler scheduler) {
            return submitOn(scheduler, null);
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
            Deferred<TOut> deferred = null;
            try {
                deferred = mThenCallback.run(new ThenParams<>(input, scope, tag));
            } catch (final InterruptedException e) {
                deferred = Defer.cancel();
                Thread.currentThread().interrupt();
            } catch (final CancellationException e) {
                deferred = Defer.cancel();
            } catch (final Exception e) {
                deferred = Defer.fail(e);
            } finally {
                if (deferred == null) {
                    deferred = Defer.fail(new NullDeferredException("ThenCallback returned null"));
                }
            }

            final Result<TOut> output = Results.exchangeCancelToken(deferred.getResult(), cancelToken);

            if (mNextPoint != null) {
                mNextPoint.execute(output, cancelToken, scope, tag);
            } else {
                if (output.getCancelToken().isCanceled()) {
                    return;
                }
                final Exception e = output.getException();
                if (e != null) {
                    throw new UnhandledException("Unhandled exception occurred.", e);
                }
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
        public Deferred<TOut> getOn(Scheduler scheduler, Tagged state) {
            final Deferrable<TOut> deferred = new Deferrable<>();

            final Canceller canceller = finish(new FinishCallback<TOut>() {
                @Override
                public void run(final FinishParams<TOut> params) {
                    deferred.setResult(params.asResult());
                }
            }).submitOn(scheduler, state.getTag());

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
        public Promise<TOut> promiseOn(final Scheduler scheduler) {
            return when(new WhenCallback<TOut>() {

                @Override
                public Deferred<TOut> run(WhenParams params) throws Exception {
                    return getOn(scheduler, params);
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
        public Canceller submitOn(Scheduler scheduler, Object tag) {
            return mEntryPoint.submitOn(scheduler, tag);
        }

        @Override
        public Canceller submitOn(Scheduler scheduler) {
            return submitOn(scheduler, null);
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
                    watchCallback.run(new WatchParams<>(params.asResult(), params.getScope(), params.getTag()));
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

    private static final class InitialPromise<TOut> implements Promise<TOut>, EntryPoint, Job {

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

            Deferred<TOut> handle = null;
            //Do not call the initial callback when Promise was already canceled.
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
                } finally {
                    if (handle == null) {
                        handle = Defer.fail(new NullDeferredException("WhenCallback returned null"));
                    }
                }
            }

            final Result<TOut> result = Results.exchangeCancelToken(handle.getResult(), cancelToken);

            if (mNextPoint != null) {
                mNextPoint.execute(result, cancelToken, scope, tag);
            } else {
                if (result.getCancelToken().isCanceled()) {
                    return;
                }

                final Exception e = result.getException();
                if (e != null) {
                    throw new UnhandledException("Promise unhandled exception occurred.", e);
                }
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
        public Deferred<TOut> getOn(Scheduler scheduler, Tagged state) {

            final Deferrable<TOut> deferred = new Deferrable<>();

            final Canceller canceller = finish(new FinishCallback<TOut>() {
                @Override
                public void run(final FinishParams<TOut> args) {
                    deferred.setResult(args.asResult());
                }
            }).submitOn(scheduler, state.getTag());

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
        public Promise<TOut> promiseOn(final Scheduler scheduler) {
            return when(new WhenCallback<TOut>() {

                @Override
                public Deferred<TOut> run(final WhenParams params) throws Exception {
                    return getOn(scheduler, params);
                }
            });
        }

        private Canceller submitOn(final ExecutorService executorService, final Object tag, final Runnable postProcess) {
            //This method is the entry point of Promise. All of the instances of other Promise eventually call this.

            final FutureCanceller canceller = new FutureCanceller(new Runnable() {
                @Override
                public void run() {
                    boolean hasCriticalError = false;
                    final ArrayCloseableStack scope = new ArrayCloseableStack();
                    try {
                        execute(CancelTokens.CANCELED, scope, tag);
                    } catch (final RuntimeException e) {
                        hasCriticalError = true;
                        Log.e("loilo-promise", "InitialPromise: Promise exception occurred on canceling.", e);
                        Dispatcher.getMainDispatcher().run(new Runnable() {
                            @Override
                            public void run() {
                                //Throw on main thread to crash the application.
                                throw e;
                            }
                        });
                    } catch (final Error e) {
                        hasCriticalError = true;
                        Log.wtf("loilo-promise", "InitialPromise: Promise error occurred on canceling.", e);
                        Dispatcher.getMainDispatcher().run(new Runnable() {
                            @Override
                            public void run() {
                                //Throw on main thread to crash the application.
                                throw e;
                            }
                        });
                    } finally {
                        try {
                            scope.close();
                        } catch (final Error e) {

                            if (!hasCriticalError) {
                                hasCriticalError = true;
                                Log.wtf("loilo-promise", "InitialPromise: Scope close error occurred on canceling.", e);
                                Dispatcher.getMainDispatcher().run(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Throw on main thread to crash the application.
                                        throw e;
                                    }
                                });
                            }
                        } finally {
                            if (!hasCriticalError && postProcess != null) {
                                postProcess.run();
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
                    } catch (final RuntimeException e) {
                        hasCriticalError = true;
                        Log.e("loilo-promise", "InitialPromise: Promise exception occurred.", e);
                        Dispatcher.getMainDispatcher().run(new Runnable() {
                            @Override
                            public void run() {
                                //Throw on main thread to crash the application.
                                throw e;
                            }
                        });
                    } catch (final Error e) {
                        hasCriticalError = true;
                        Log.wtf("loilo-promise", "InitialPromise: Promise error occurred.", e);
                        Dispatcher.getMainDispatcher().run(new Runnable() {
                            @Override
                            public void run() {
                                //Throw on main thread to crash the application.
                                throw e;
                            }
                        });
                    } finally {
                        try {
                            scope.close();
                        } catch (final Error e) {

                            if (!hasCriticalError) {
                                hasCriticalError = true;
                                Log.wtf("loilo-promise", "InitialPromise: Scope close error occurred.", e);
                                Dispatcher.getMainDispatcher().run(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Throw on main thread to crash the application.
                                        throw e;
                                    }
                                });
                            }
                        } finally {
                            if (!hasCriticalError && postProcess != null) {
                                postProcess.run();
                            }
                        }
                    }
                }
            });
            canceller.setFuture(future);
            return canceller;
        }


        @Override
        public Canceller submitOn(final ExecutorService executorService, final Object tag) {
            return submitOn(executorService, tag, null);
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
        public Canceller doWork(Object tag, Runnable postProcess) {
            return submitOn(mDefaultExecutorService, tag, postProcess);
        }

        @Override
        public void giveUp(final Object tag) {
            mDefaultExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    boolean hasCriticalError = false;
                    final ArrayCloseableStack scope = new ArrayCloseableStack();
                    try {
                        execute(CancelTokens.CANCELED, scope, tag);
                    } catch (final RuntimeException e) {
                        hasCriticalError = true;
                        Log.e("loilo-promise", "InitialPromise: Promise exception occurred on canceling.", e);
                        Dispatcher.getMainDispatcher().run(new Runnable() {
                            @Override
                            public void run() {
                                //Throw on main thread to crash the application.
                                throw e;
                            }
                        });
                    } catch (final Error e) {
                        hasCriticalError = true;
                        Log.wtf("loilo-promise", "InitialPromise: Promise error occurred on canceling.", e);
                        Dispatcher.getMainDispatcher().run(new Runnable() {
                            @Override
                            public void run() {
                                //Throw on main thread to crash the application.
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
                                        //Throw on main thread to crash the application.
                                        throw e;
                                    }
                                });
                            }
                        }
                    }
                }
            });
        }

        @Override
        public Canceller submitOn(Scheduler scheduler, Object tag) {
            return scheduler.post(this, tag);
        }

        @Override
        public Canceller submitOn(Scheduler scheduler) {
            return submitOn(scheduler, null);
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
                    watchCallback.run(new WatchParams<>(params.asResult(), params.getScope(), params.getTag()));
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
