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
import android.support.v4.app.Fragment;

import tv.loilo.promise.Promise;

/**
 * Interface for the creation of tv.loilo.promise.Promise.
 *
 * @param <TFragment> the specified type of a implementation of {@link ProgressPromiseLoaderCallbacks}
 * @param <TData>     the specified type of a success value
 * @param <TProgress> the specified type of progress value
 */
public interface ProgressPromiseFactory<TFragment extends Fragment & ProgressPromiseLoaderCallbacks<TData, TProgress>, TData, TProgress> {
    @NonNull
    Promise<TData> createPromise(@NonNull ProgressPromiseLoader<TFragment, TData, TProgress> loader);
}
