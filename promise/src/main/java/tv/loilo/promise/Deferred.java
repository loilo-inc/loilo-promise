package tv.loilo.promise;

/**
 * Created by Junpei on 2015/06/16.
 */
public interface Deferred<T> {
    Result<T> getResult();
}
