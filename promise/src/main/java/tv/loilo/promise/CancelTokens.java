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
 * The fixed states of cancellation.
 */
public final class CancelTokens {

    /**
     * The token that is always canceled.
     */
    public static final CancelToken CANCELED = new StaticCancelToken(true);

    /**
     * The token that is always not canceled.
     */
    public static final CancelToken NONE = new StaticCancelToken(false);

    private CancelTokens() {
    }

    private static class StaticCancelToken implements CancelToken {
        private final boolean mIsCanceled;

        public StaticCancelToken(boolean isCanceled) {
            mIsCanceled = isCanceled;
        }

        @Override
        public boolean isCanceled() {
            return mIsCanceled;
        }
    }
}
