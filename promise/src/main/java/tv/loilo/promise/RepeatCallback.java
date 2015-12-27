package tv.loilo.promise;

/**
 * Created by pepeotoito on 2015/12/27.
 */
public interface RepeatCallback<TOut> {
    Deferred<TOut> run(RepeatParams params) throws Exception;
}
