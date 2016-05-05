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
 * Result of asynchronous processing.
 * The result will take one of the states of the success or failure or canceled.
 *
 * Access in the followings order.
 * 1. {@link Result#getCancelToken() getCancelToken()}
 * 2. {@link Result#getException() getException()}
 * 3. {@link Result#getValue() getValue()}
 */
public interface Result<T> extends CancelState {

    /**
     * Returns the course of the task failure or {@code null} if the task is not failed.
     *
     * @return the course of the task failure or {@code null} if the task is not failed
     */
    Exception getException();

    /**
     * Returns {@code true} if the value is set or {@code false} if the value is not set.
     *
     * @return {@code true} if the value is set or {@code false} if the value is not set
     */
    boolean hasValue();

    /**
     * Returns the value if the task is succeeded or {@code null} if the task is not succeeded or the value is set {@code null}.
     *
     * @return the value if the task is succeeded or {@code null} if the task is not succeeded or the value is set {@code null}
     */
    T getValue();

    /**
     * Returns the value if the task is succeeded or throws exception otherwise.
     *
     * @return the value if the task is succeeded
     * @throws java.util.concurrent.CancellationException the task is canceled
     * @throws java.lang.Exception                        the cause of the task failure
     */
    T safeGetValue() throws Exception;
}
