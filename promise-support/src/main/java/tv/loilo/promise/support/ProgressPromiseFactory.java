package tv.loilo.promise.support;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import tv.loilo.promise.Promise;

/**
 * Created by pepeotoito on 2015/12/26.
 */
public interface ProgressPromiseFactory<TFragment extends Fragment & ProgressPromiseLoaderCallbacks<TData, TProgress>, TData, TProgress> {
    @NonNull
    Promise<TData> createPromise(@NonNull ProgressPromiseLoader<TFragment, TData, TProgress> loader);
}
