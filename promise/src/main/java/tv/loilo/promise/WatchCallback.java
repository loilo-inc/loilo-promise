package tv.loilo.promise;

/**
 * Created by Junpei on 2015/12/21.
 */
public interface WatchCallback<T> {
    void run(final ResultParams<T> params) throws Exception;
}
