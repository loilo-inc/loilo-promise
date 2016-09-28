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
 * Interface to determine whether repeated continue or end.
 */
public interface UntilCallback<TIn> {
    /**
     * Determine whether repeated continue or end.
     *
     * @param params the parameters contains the result of the previous {@link RepeatCallback}
     * @return {@code true} if you want to stop repeat and {@code false} if you want to continue repeat
     * @throws Exception any
     */
    Deferred<Boolean> run(UntilParams<TIn> params) throws Exception;
}
