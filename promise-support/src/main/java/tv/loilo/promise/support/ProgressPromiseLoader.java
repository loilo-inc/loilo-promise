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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;

import tv.loilo.promise.Dispatcher;
import tv.loilo.promise.Promise;
import tv.loilo.promise.Result;
import tv.loilo.promise.Transfer;

/**
 * Base class for android.support.v4.content.Loader for using tv.loilo.promise.Promise with the progress notification.
 *
 * @param <TFragment> the specified type of a implementation of {@link ProgressPromiseLoaderCallbacks}
 * @param <TData>     the specified type of a success value
 * @param <TProgress> the specified type of progress value
 */
public abstract class ProgressPromiseLoader<TFragment extends Fragment & ProgressPromiseLoaderCallbacks<TData, TProgress>, TData, TProgress>
        extends PromiseLoader<TData> {

    @Nullable
    private TFragment mParentFragment;
    @Nullable
    private TProgress mProgressCache;

    public ProgressPromiseLoader(@NonNull TFragment fragment) {
        super(fragment.getContext());
        attachFragment(fragment);
    }

    @NonNull
    public static <TFragment extends Fragment & ProgressPromiseLoaderCallbacks<TData, TProgress>, TData, TProgress>
    ProgressPromiseLoader<TFragment, TData, TProgress> createLoader(
            @NonNull TFragment fragment,
            @NonNull final ProgressPromiseFactory<TFragment, TData, TProgress> promiseFactory) {
        return new ProgressPromiseLoader<TFragment, TData, TProgress>(fragment) {
            @NonNull
            @Override
            protected Promise<TData> onCreatePromise() throws Exception {
                return promiseFactory.createPromise(this);
            }
        };
    }

    @NonNull
    public static <TFragment extends Fragment & ProgressPromiseLoaderCallbacks<TData, TProgress>, TData, TProgress>
    ProgressPromiseLoader<TFragment, TData, TProgress> createLoader(
            @NonNull TFragment fragment,
            @NonNull final ProgressPromiseFactory<TFragment, TData, TProgress> promiseFactory,
            @Nullable final PromiseCacheCleaner<TData> dataCacheCleaner) {
        return new ProgressPromiseLoader<TFragment, TData, TProgress>(fragment) {
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

    public static <TLoader extends ProgressPromiseLoader<TFragment, TData, TProgress>, TFragment extends Fragment & ProgressPromiseLoaderCallbacks<TData, TProgress>, TData, TProgress>
    void attachLoader(int id, @NonNull TFragment fragment) {
        final Loader<Result<TData>> loader = fragment.getLoaderManager().getLoader(id);
        if (loader == null) {
            return;
        }

        @SuppressWarnings("unchecked") final TLoader promiseLoader = (TLoader) loader;
        promiseLoader.attachFragment(fragment);
    }

    public static <TLoader extends ProgressPromiseLoader<TFragment, TData, TProgress>, TFragment extends Fragment & ProgressPromiseLoaderCallbacks<TData, TProgress>, TData, TProgress>
    void detachLoader(int id, @NonNull TFragment fragment) {
        final Loader<Result<TData>> loader = fragment.getLoaderManager().getLoader(id);
        if (loader == null) {
            return;
        }

        @SuppressWarnings("unchecked") final TLoader promiseLoader = (TLoader) loader;
        promiseLoader.detachFragment();
    }

    void attachFragment(@NonNull TFragment fragment) {
        if (fragment.isResumed() && fragment != mParentFragment) {
            detachFragment();
            mParentFragment = fragment;
            notifyProgress();
        }
    }

    void detachFragment() {
        mParentFragment = null;
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
        if (mParentFragment != null && mProgressCache != null) {
            mParentFragment.onLoaderProgress(getId(), mProgressCache);
        }
    }
}
