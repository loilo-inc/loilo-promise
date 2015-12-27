package tv.loilo.promise;

/**
 * Created by Junpei on 2015/06/12.
 */
public interface Continuation<TIn, TOut> {
    Deferred<TOut> run(final ResultParams<TIn> params) throws Exception;
}
