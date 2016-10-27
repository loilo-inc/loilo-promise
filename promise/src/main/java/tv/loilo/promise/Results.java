/*
 * Copyright (c) 2015-2016 LoiLo inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.loilo.promise;

import java.util.concurrent.CancellationException;

/**
 * Class to make a {@link Result}.
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

    public static <T> Result<T> fail(Throwable e) {
        return new SimpleResult<>(false, null, e, CancelTokens.NONE);
    }

    public static <T> Result<T> notImpl() {
        return new SimpleResult<>(false, null, new UnsupportedOperationException("Not implemented."), CancelTokens.NONE);
    }

    public static <TIn, TOut> Result<TOut> exchangeValue(Result<TIn> result, TOut replace) {
        if (result.getCancelToken().isCanceled()) {
            return cancel();
        }
        Throwable e;
        try {
            e = result.getException();
        } catch (final Error error) {
            e = error;
        }

        if (e != null) {
            return fail(e);
        }
        return success(replace);
    }

    public static <T> Result<T> exchangeCancelToken(Result<T> result, CancelToken cancelToken) {
        if (result.getCancelToken().isCanceled()) {
            return result;
        }

        final boolean hasValue = result.hasValue();
        final T value = result.getValue();
        Throwable e;
        try {
            e = result.getException();
        } catch (final Error error) {
            e = error;
        }

        //Exchanges cancellationState.
        return new SimpleResult<>(hasValue, value, e, cancelToken);
    }

    private static class SimpleResult<T> implements Result<T> {

        private final boolean mHasValue;
        private final T mValue;
        private final Throwable mException;
        private final CancelToken mCancelToken;

        private SimpleResult(
                boolean hasValue, T value,
                Throwable exception,
                CancelToken cancelToken) {
            mHasValue = hasValue;
            mValue = value;
            mException = exception;
            mCancelToken = cancelToken;
        }

        @Override
        public Exception getException() {
            if (mException == null) {
                return null;
            }
            if (mException instanceof Exception) {
                return (Exception) mException;
            }
            throw (Error) mException;
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
                    if (mException instanceof Exception) {
                        throw (Exception) mException;
                    }
                    throw (Error) mException;
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
