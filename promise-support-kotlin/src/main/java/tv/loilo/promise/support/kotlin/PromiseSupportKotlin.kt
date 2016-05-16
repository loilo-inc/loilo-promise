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

package tv.loilo.promise.support.kotlin

import android.content.Context
import android.support.v4.app.LoaderManager
import tv.loilo.promise.Promise
import tv.loilo.promise.support.ProgressPromiseLoader
import tv.loilo.promise.support.ProgressPromiseLoaderCallbacks
import tv.loilo.promise.support.PromiseLoader

fun LoaderManager.cancelLoader(id: Int) {
    PromiseLoader.cancelLoader(this, id)
}

fun <TData> createPromiseLoader(
        context: Context,
        createPromise: (PromiseLoader<TData>) -> Promise<TData>,
        clearDataCache: ((TData) -> Unit)? = null): PromiseLoader<TData> {
    return object : PromiseLoader<TData>(context) {
        override fun onCreatePromise(): Promise<TData> {
            return createPromise(this)
        }

        override fun onClearDataCache(cache: TData) {
            clearDataCache?.invoke(cache)
        }
    }
}

fun <TData, TProgress> createProgressPromiseLoader(
        context: Context,
        loaderCallbacks: ProgressPromiseLoaderCallbacks<TData, TProgress>,
        shouldAttachProgressCallback: Boolean,
        createPromise: (ProgressPromiseLoader<TData, TProgress>) -> Promise<TData>,
        clearDataCache: ((TData) -> Unit)? = null)
        : ProgressPromiseLoader<TData, TProgress> {
    return ProgressPromiseLoader.createLoader(context, loaderCallbacks, shouldAttachProgressCallback, createPromise, clearDataCache)
}

fun <TData, TProgress> LoaderManager.attachProgressCallback(id: Int, loaderCallbacks: ProgressPromiseLoaderCallbacks<TData, TProgress>) {
    ProgressPromiseLoader.attachProgressCallback(this, id, loaderCallbacks)
}

fun <TData, TProgress> LoaderManager.detachProgressCallback(id: Int, loaderCallbacks: ProgressPromiseLoaderCallbacks<TData, TProgress>) {
    ProgressPromiseLoader.detachProgressCallback(this, id, loaderCallbacks);
}
