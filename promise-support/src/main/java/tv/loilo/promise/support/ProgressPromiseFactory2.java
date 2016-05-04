package tv.loilo.promise.support;

import android.support.annotation.NonNull;

import tv.loilo.promise.Promise;

public interface ProgressPromiseFactory2<TData, TProgress> {
    @NonNull
    Promise<TData> createPromise(@NonNull ProgressPromiseLoader2<TData, TProgress> loader);
}
