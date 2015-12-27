package tv.loilo.promise;

/**
 * Created by pepeotoito on 2015/12/27.
 */
public class UntilParams<T> extends ResultParams<T> {

    public UntilParams(Result<T> result, CloseableStack scope, Object tag) {
        super(result, scope, tag);
    }
}
