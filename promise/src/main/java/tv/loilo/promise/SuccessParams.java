package tv.loilo.promise;

/**
 * Created by Junpei on 2015/10/26.
 */
public class SuccessParams<T> implements ExecutionContext {
    private final T mValue;

    private final CancelToken mCancelToken;

    private final CloseableStack mScope;

    private final Object mTag;

    public SuccessParams(T value, CancelToken cancelToken, CloseableStack scope, Object tag) {
        mValue = value;
        mCancelToken = cancelToken;
        mScope = scope;
        mTag = tag;
    }

    public T getValue() {
        return mValue;
    }


    @Override
    public CloseableStack getScope() {
        return mScope;
    }


    @Override
    public Object getTag() {
        return mTag;
    }


    @Override
    public CancelToken getCancelToken() {
        return mCancelToken;
    }


    public Deferred<T> asDeferred() {
        return Defer.success(mValue);
    }
}
