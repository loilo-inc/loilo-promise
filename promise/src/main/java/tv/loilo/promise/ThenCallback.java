package tv.loilo.promise;

/**
 * Created by Junpei on 2015/06/12.
 */
public interface ThenCallback<TIn, TOut> {
    Deferred<TOut> run(ThenParams<TIn> params) throws Exception;
}
