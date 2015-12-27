package tv.loilo.promise;

/**
 * Created by Junpei on 2015/10/26.
 */
public class ResultParams<T> implements ExecutionContext {

    private final Result<T> mResult;

    private final CloseableStack mScope;

    private final Object mTag;

    public ResultParams(Result<T> result, CloseableStack scope, Object tag) {
        mResult = result;
        mScope = scope;
        mTag = tag;
    }

    public Exception getException() {
        return mResult.getException();
    }

    public boolean hasValue() {
        return mResult.hasValue();
    }

    public T getValue() {
        return mResult.getValue();
    }

    public T safeGetValue() throws Exception {
        return mResult.safeGetValue();
    }


    @Override
    public Object getTag() {
        return mTag;
    }


    @Override
    public CloseableStack getScope() {
        return mScope;
    }


    @Override
    public CancelToken getCancelToken() {
        return mResult.getCancelToken();
    }


    public Deferred<T> asDeferred() {
        return Defer.complete(mResult);
    }

    public Result<T> asResult() {
        return mResult;
    }
}
