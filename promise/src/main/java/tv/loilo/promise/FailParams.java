/*
 * Copyright 2015 LoiLo inc.
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
