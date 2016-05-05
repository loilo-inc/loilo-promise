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

package tv.loilo.promise.support;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import tv.loilo.promise.Dispatcher;
import tv.loilo.promise.Promise;
import tv.loilo.promise.Result;
import tv.loilo.promise.Transfer;

public abstract class ProgressPromiseLoader2<TData, TProgress> extends PromiseLoader<TData> {

    @Nullable
    private ProgressPromiseLoaderCallbacks<TData, TProgress> mLoaderCallbacks;

    @Nullable
    private TProgress mProgressCache;

    public ProgressPromiseLoader2(Context context) {
        super(context);
    }

    @NonNull
    public static <TData, TProgress> ProgressPromiseLoader2<TData, TProgress> createLoader(
            Context context,
            @NonNull final ProgressPromiseFactory2<TData, TProgress> promiseFactory) {
        return new ProgressPromiseLoader2<TData, TProgress>(context) {
            @NonNull
            @Override
            protected Promise<TData> onCreatePromise() throws Exception {
                return promiseFactory.createPromise(this);
            }
        };
    }

    @NonNull
    public static <TData, TProgress> ProgressPromiseLoader2<TData, TProgress> createLoader(
            Context context,
            @NonNull final ProgressPromiseFactory2<TData, TProgress> promiseFactory,
            @Nullable final PromiseCacheCleaner<TData> dataCacheCleaner) {
        return new ProgressPromiseLoader2<TData, TProgress>(context) {
            @NonNull
            @Override
            protected Promise<TData> onCreatePromise() throws Exception {
                return promiseFactory.createPromise(this);
            }

            @Override
            protected void onClearDataCache(@NonNull TData cache) {
                if (dataCacheCleaner != null) {
                    dataCacheCleaner.onClearDataCache(cache);
                }
            }
        };
    }

    public static <TData, TProgress> void attachProgressCallback(LoaderManager loaderManager, int id, @NonNull ProgressPromiseLoaderCallbacks<TData, TProgress> loaderCallbacks) {
        final Loader<Result<TData>> loader = loaderManager.getLoader(id);
        if (loader == null) {
            return;
        }

        @SuppressWarnings("unchecked") final ProgressPromiseLoader2<TData, TProgress> promiseLoader = (ProgressPromiseLoader2<TData, TProgress>) loader;
        promiseLoader.attachLoaderCallbacks(loaderCallbacks);
    }

    public static <TData, TProgress> void detachProgressCallback(LoaderManager loaderManager, int id, @NonNull ProgressPromiseLoaderCallbacks<TData, TProgress> loaderCallbacks) {
        final Loader<Result<TData>> loader = loaderManager.getLoader(id);
        if (loader == null) {
            return;
        }

        @SuppressWarnings("unchecked") final ProgressPromiseLoader2<TData, TProgress> promiseLoader = (ProgressPromiseLoader2<TData, TProgress>) loader;
        promiseLoader.detachLoaderCallbacks(loaderCallbacks);
    }

    private void attachLoaderCallbacks(ProgressPromiseLoaderCallbacks<TData, TProgress> loaderCallbacks) {
        if (loaderCallbacks != mLoaderCallbacks) {
            detachLoaderCallbacks(mLoaderCallbacks);
            mLoaderCallbacks = loaderCallbacks;
            notifyProgress();
        }
    }

    private void detachLoaderCallbacks(ProgressPromiseLoaderCallbacks<TData, TProgress> loaderCallbacks) {
        if (loaderCallbacks == mLoaderCallbacks) {
            mLoaderCallbacks = null;
        }
    }

    @Override
    protected void clearCaches() {
        super.clearCaches();

        mProgressCache = null;
    }

    public void reportProgress(@NonNull final Transfer<TProgress> transferProgress) {
        Dispatcher.getMainDispatcher().run(new Runnable() {
            @Override
            public void run() {
                if (transferProgress.getCancelToken().isCanceled()) {
                    return;
                }
                mProgressCache = transferProgress.getData();
                notifyProgress();
            }
        });
    }

    private void notifyProgress() {
        if (mLoaderCallbacks != null && mProgressCache != null) {
            mLoaderCallbacks.onLoaderProgress(getId(), mProgressCache);
        }
    }
}
