package tv.loilo.promise;

/**
 * Created by pepeotoito on 2015/12/27.
 */
public class WatchParams<TOut> extends ResultParams<TOut> {
    public WatchParams(Result<TOut> result, CloseableStack scope, Object tag) {
        super(result, scope, tag);
    }
}
