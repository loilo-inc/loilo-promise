package tv.loilo.promise.support.kotlin

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import tv.loilo.promise.Promise
import tv.loilo.promise.support.ProgressPromiseLoader
import tv.loilo.promise.support.ProgressPromiseLoaderCallbacks
import tv.loilo.promise.support.PromiseLoader

/**
 * Created by pepeotoito on 2015/12/27.
 */

fun LoaderManager.cancelLoader(id: Int) {
    PromiseLoader.cancelLoader(this, id)
}

fun <TData> createPromiseLoader(
        context: Context,
        createPromise: () -> Promise<TData>,
        clearDataCache: ((TData) -> Unit)? = null): PromiseLoader<TData> {
    return object : PromiseLoader<TData>(context) {
        override fun onCreatePromise(): Promise<TData>? {
            return createPromise()
        }

        override fun onClearDataCache(cache: TData) {
            clearDataCache?.invoke(cache)
        }
    }
}

fun <TLoader, TFragment, TData, TProgress> createProgressPromiseLoader(
        fragment: TFragment,
        createPromise: (ProgressPromiseLoader<TFragment, TData, TProgress>) -> Promise<TData>,
        clearDataCache: ((TData) -> Unit)? = null): ProgressPromiseLoader<TFragment, TData, TProgress>
        where TFragment : Fragment, TFragment : ProgressPromiseLoaderCallbacks<TData, TProgress>, TLoader : ProgressPromiseLoader<TFragment, TData, TProgress> {
    return ProgressPromiseLoader.createLoader(fragment, createPromise, clearDataCache)
}

fun <TLoader, TFragment, TData, TProgress> attachProgressPromiseLoader(id: Int, fragment: TFragment)
        where TFragment : Fragment, TFragment : ProgressPromiseLoaderCallbacks<TData, TProgress>, TLoader : ProgressPromiseLoader<TFragment, TData, TProgress> {
    ProgressPromiseLoader.attachLoader(id, fragment)
}

fun <TLoader, TFragment, TData, TProgress> detachProgressPromiseLoader(id: Int, fragment: TFragment)
        where TFragment : Fragment, TFragment : ProgressPromiseLoaderCallbacks<TData, TProgress>, TLoader : ProgressPromiseLoader<TFragment, TData, TProgress> {
    ProgressPromiseLoader.detachLoader(id, fragment)
}
