package tv.loilo.promise;

/**
 * Created by pepeotoito on 2015/12/27.
 */
public class ForEachParams<T> extends SuccessParams<T> {
    public ForEachParams(T value, CancelToken cancelToken, CloseableStack scope, Object tag) {
        super(value, cancelToken, scope, tag);
    }
}
