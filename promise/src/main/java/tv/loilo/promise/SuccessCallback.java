package tv.loilo.promise;

/**
 * Created by Junpei on 2015/06/16.
 */
public interface SuccessCallback<TIn, TOut> {

    Deferred<TOut> run(SuccessParams<TIn> params) throws Exception;
}
