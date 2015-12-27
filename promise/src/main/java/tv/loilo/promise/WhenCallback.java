package tv.loilo.promise;

/**
 * Created by Junpei on 2015/06/12.
 */
public interface WhenCallback<TOut> {
    Deferred<TOut> run(WhenParams params) throws Exception;
}
