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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Parameters that are passed to {@link ForEachCallback}.
 */
public class ForEachParams<TValue, TOperand> extends SuccessParams<TValue> {
    private final AtomicInteger mIndex;
    private final TOperand mOperand;

    public ForEachParams(AtomicInteger index, TValue value, TOperand operand, CancelToken cancelToken, CloseableStack scope, Object tag) {
        super(value, cancelToken, scope, tag);
        mIndex = index;
        mOperand = operand;
    }

    public AtomicInteger getIndex() {
        return mIndex;
    }

    public TOperand getOperand() {
        return mOperand;
    }
}
