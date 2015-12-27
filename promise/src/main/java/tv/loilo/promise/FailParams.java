package tv.loilo.promise;

/**
 * Created by Junpei on 2015/10/26.
 */
public class FailParams<T> implements ExecutionContext {
    private final Exception mException;
    private final CancelToken mCancelToken;
    private final CloseableStack mScope;
    private final Object mTag;

    public FailParams(Exception exception, CancelToken cancelToken, CloseableStack scope, Object tag) {
        mException = exception;
        mCancelToken = cancelToken;
        mScope = scope;
        mTag = tag;
    }

    public Exception getException() {
        return mException;
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
        return Defer.fail(mException);
    }
}
