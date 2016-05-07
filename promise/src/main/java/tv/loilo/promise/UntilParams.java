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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Parameters that are passed to {@link UntilCallback}.
 */
public final class UntilParams<T> extends ResultParams<T> {

    private final AtomicInteger mIndex;

    public UntilParams(AtomicInteger index, Result<T> result, CloseableStack scope, Object tag) {
        super(result, scope, tag);
        mIndex = index;
    }

    public AtomicInteger getIndex() {
        return mIndex;
    }
}
