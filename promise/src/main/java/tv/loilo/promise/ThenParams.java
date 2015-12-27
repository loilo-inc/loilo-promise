package tv.loilo.promise;

/**
 * Created by pepeotoito on 2015/12/27.
 */
public class ThenParams<TOut> extends ResultParams<TOut> {
    public ThenParams(Result<TOut> result, CloseableStack scope, Object tag) {
        super(result, scope, tag);
    }
}
