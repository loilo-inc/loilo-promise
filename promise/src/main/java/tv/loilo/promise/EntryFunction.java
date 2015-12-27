package tv.loilo.promise;

/**
 * Created by Junpei on 2015/06/12.
 */
public interface EntryFunction<TOut> {
    Deferred<TOut> run(final EntryParams params) throws Exception;
}
