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

/**
 * Extends android.support.v4.app.LoaderManager.LoaderCallbacks for {@link ProgressPromiseLoader}.
 *
 * @param <TData>     the specified type of a success value
 * @param <TProgress> the specified type of progress value
 */
public interface ProgressPromiseLoaderCallbacks<TData, TProgress> extends PromiseLoaderCallbacks<TData> {
    void onLoaderProgress(int id, @NonNull TProgress progress);
}
