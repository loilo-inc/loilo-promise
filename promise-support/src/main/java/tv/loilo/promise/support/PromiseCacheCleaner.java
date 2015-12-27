package tv.loilo.promise.support;

import android.support.annotation.NonNull;

/**
 * Created by pepeotoito on 2015/12/27.
 */
public interface PromiseCacheCleaner<TData> {
    void onClearDataCache(@NonNull TData cache);
}
