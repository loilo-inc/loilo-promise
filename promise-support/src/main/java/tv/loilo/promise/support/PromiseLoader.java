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

package tv.loilo.promise.support;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import tv.loilo.promise.Canceller;
import tv.loilo.promise.Dispatcher;
import tv.loilo.promise.FinishCallback;
import tv.loilo.promise.FinishParams;
import tv.loilo.promise.Promise;
import tv.loilo.promise.Promises;
import tv.loilo.promise.Result;
import tv.loilo.promise.Results;

/**
 * Created by pepeotoito on 2015/12/26.
 */
public abstract class PromiseLoader<TData> extends Loader<Result<TData>> {

    @Nullable
    private Canceller mCanceller;
    private boolean mIsCanceling;
    private boolean mIsPending;

    @Nullable
    private TData mDataCache;

    /**
     * Stores away the application context associated with context.
     * Since Loaders can be used across multiple activities it's dangerous to
     * store the context directly; always use {@link #getContext()} to retrieve
     * the Loader's Context, don't use the constructor argument directly.
     * The Context returned by {@link #getContext} is safe to use across
     * Activity instances.
     *
     * @param context used to retrieve the application context.
     */
    public PromiseLoader(Context context) {
        super(context);
    }

    public static void cancelLoader(LoaderManager loaderManager, int id) {
        final Loader<?> loader = loaderManager.getLoader(id);
        if (loader == null) {
            return;
        }
        loader.cancelLoad();
    }

    @NonNull
    protected abstract Promise<TData> onCreatePromise() throws Exception;

    @SuppressWarnings("unused")
    protected void onClearDataCache(@NonNull TData cache) {

    }

    protected void clearCaches() {
        if (mDataCache != null) {
            onClearDataCache(mDataCache);
            mDataCache = null;
        }
    }

    @NonNull
    private Promise<TData> getPromise() {
        try {
            return onCreatePromise();
        } catch (final Exception e) {
            return Promises.fail(e);
        }
    }

    private boolean cancelPromise() {
        final Canceller canceller = mCanceller;
        mCanceller = null;
        mIsPending = false;
        if (canceller == null) {
            return mIsCanceling;
        }
        mIsCanceling = true;
        canceller.cancel();
        return mIsCanceling;
    }

    @Override
    protected boolean onCancelLoad() {
        return cancelPromise();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onForceLoad() {

        if (cancelLoad()) {
            mIsPending = true;
            return;
        }

        clearCaches();

        mCanceller = getPromise().finish(new FinishCallback<TData>() {
            @Override
            public void run(final FinishParams<TData> params) {
                Dispatcher.getMainDispatcher().run(new Runnable() {
                    @Override
                    public void run() {
                        mIsCanceling = false;

                        if (params.getCancelToken().isCanceled()) {
                            final TData value = params.getValue();
                            if (value != null) {
                                onClearDataCache(value);
                            }

                            if (!isAbandoned()) {
                                rollbackContentChanged();
                                deliverResult(Results.<TData>cancel());
                                if (mIsPending && isStarted()) {
                                    forceLoad();
                                }
                            }

                            return;
                        }

                        mCanceller = null;

                        final Exception e = params.getException();
                        if (e != null) {
                            if (!isAbandoned()) {
                                rollbackContentChanged();
                                deliverResult(Results.<TData>fail(e));
                            }
                        } else {

                            final TData value = params.getValue();
                            if (!isAbandoned()) {
                                mDataCache = value;
                                commitContentChanged();
                                deliverResult(Results.success(value));
                            } else {
                                if (value != null) {
                                    onClearDataCache(value);
                                }
                            }
                        }
                    }
                });
            }
        }).submit();
    }

    @Override
    protected void onStartLoading() {
        if (mDataCache != null) {
            deliverResult(Results.success(mDataCache));
        }

        if (takeContentChanged() || mDataCache == null) {
            forceLoad();
        }
    }

    @Override
    protected void onReset() {
        onStopLoading();

        clearCaches();
    }
}
