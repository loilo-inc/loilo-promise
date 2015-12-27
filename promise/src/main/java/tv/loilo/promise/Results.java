package tv.loilo.promise;

import java.util.concurrent.CancellationException;

/**
 * Created by Junpei on 2015/06/12.
 */
public final class Results {
    private Results() {

    }

    public static <T> Result<T> success(T value) {
        return new SimpleResult<>(true, value, null, CancelTokens.NONE);
    }

    public static <T> Result<T> cancel() {
        return new SimpleResult<>(false, null, null, CancelTokens.CANCELED);
    }

    public static <T> Result<T> fail(Exception e) {
        return new SimpleResult<>(false, null, e, CancelTokens.NONE);
    }

    public static <T> Result<T> notImpl() {
        return new SimpleResult<>(false, null, new UnsupportedOperationException("Not implementation."), CancelTokens.NONE);
    }

    public static <TIn, TOut> Result<TOut> exchangeValue(Result<TIn> result, TOut replace) {
        if (result.getCancelToken().isCanceled()) {
            return cancel();
        }
        final Exception e = result.getException();
        if (e != null) {
            return fail(e);
        }
        return success(replace);
    }

    public static <T> Result<T> exchangeCancelToken(Result<T> result, CancelToken cancelToken) {
        if (result.getCancelToken().isCanceled()) {
            //キャンセル済みだったらそのまま返していい。
            return result;
        }

        final boolean hasValue = result.hasValue();
        final T value = result.getValue();
        final Exception error = result.getException();
        //cancellationStateを入れ替え
        return new SimpleResult<>(hasValue, value, error, cancelToken);
    }

    private static class SimpleResult<T> implements Result<T> {

        private final boolean mHasValue;
        private final T mValue;
        private final Exception mException;
        private final CancelToken mCancelToken;

        private SimpleResult(
                boolean hasValue, T value,
                Exception exception,
                CancelToken cancelToken) {
            mHasValue = hasValue;
            mValue = value;
            mException = exception;
            mCancelToken = cancelToken;
        }

        @Override
        public Exception getException() {
            return mException;
        }

        @Override
        public boolean hasValue() {
            return mHasValue;
        }

        @Override
        public T getValue() {
            return mValue;
        }

        @Override
        public T safeGetValue() throws Exception {
            if (!mHasValue) {
                if (mCancelToken.isCanceled()) {
                    throw new CancellationException();
                }

                if (mException != null) {
                    throw mException;
                }

                throw new NullPointerException();
            }
            return mValue;
        }

        @Override
        public CancelToken getCancelToken() {
            return mCancelToken;
        }
    }
}
