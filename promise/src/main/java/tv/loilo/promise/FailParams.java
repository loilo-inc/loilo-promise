package tv.loilo.promise;

/**
 * Created by Junpei on 2015/10/26.
 */
public final class FailParams<T> implements ExecutionContext {
    private final Exception mException;
    private final CancelToken mCancelToken;
    private final CloseableStack mScope;
    private final Object mTag;

    public FailParams(final Exception exception, final CancelToken cancelToken, final CloseableStack scope, final Object tag) {
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
