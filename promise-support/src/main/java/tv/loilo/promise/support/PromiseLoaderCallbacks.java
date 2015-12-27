package tv.loilo.promise.support;

import android.support.v4.app.LoaderManager;

import tv.loilo.promise.Result;

/**
 * Created by pepeotoito on 2015/12/26.
 */
public interface PromiseLoaderCallbacks<TData> extends LoaderManager.LoaderCallbacks<Result<TData>> {
}
