package tv.loilo.promise.support;

import android.support.annotation.NonNull;

/**
 * Created by pepeotoito on 2015/12/26.
 */
public interface ProgressPromiseLoaderCallbacks<TData, TProgress> extends PromiseLoaderCallbacks<TData> {
    void onLoaderProgress(int id, @NonNull TProgress progress);
}
