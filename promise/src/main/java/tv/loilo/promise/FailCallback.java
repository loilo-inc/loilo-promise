package tv.loilo.promise;

/**
 * Created by Junpei on 2015/06/16.
 */
public interface FailCallback<TOut> {
    Deferred<TOut> run(FailParams<TOut> params) throws Exception;
}
