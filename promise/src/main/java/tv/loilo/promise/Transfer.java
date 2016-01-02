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
 * Class for the data transfer.
 * Uses in progress notification, etc.
 */
public class Transfer<TData> implements TaggedCancelState {

    final Object mTag;
    private final CancelToken mCancelToken;
    private final TData mData;

    public Transfer(TaggedCancelState cancelState, TData data) {
        mCancelToken = cancelState.getCancelToken();
        mTag = cancelState.getTag();
        mData = data;
    }

    public TData getData() {
        return mData;
    }

    @Override
    public CancelToken getCancelToken() {
        return mCancelToken;
    }

    @Override
    public Object getTag() {
        return mTag;
    }
}
