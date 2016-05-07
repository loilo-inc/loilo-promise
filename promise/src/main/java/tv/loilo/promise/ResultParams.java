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

/**
 * Parameters that are passed to the chained callback in {@link Promise}.
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
