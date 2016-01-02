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

import java.util.concurrent.ExecutorService;

/**
 * Interface of promise to return a result value.
 * Promise can set callbacks to receive a result.
 * When you called one of {@link Submittable} methods, Promise will be executing asynchronously on background thread.
 */
public interface Promise<TOut> extends Submittable {

    /**
     * Executes this promise and returns a deferred result value.
     * Use this when you want to execute a Promise on any Promises callbacks.
     *
     * @param state the state of current execution context
     * @return a deferred result value
     */
    Deferred<TOut> get(TaggedCancelState state);

    /**
     * Executes this promise on a specified {@link java.util.concurrent.ExecutorService} and returns a deferred result value.
     * Use this when you want to execute a Promise on any Promises callbacks.
     *
     * @param executorService to submit a task for promise execution
     * @param state           the state of current execution context
     * @return a deferred result value
     */
    Deferred<TOut> getOn(ExecutorService executorService, Tagged state);

    /**
     * Promise to execute this promise on a specified ExecutorService.
     *
     * @param executorService to submit a task for promise execution
     * @return promise to execute on a specified ExecutorService
     */
    Promise<TOut> promiseOn(ExecutorService executorService);

    /**
     * Sets the callback when this promise is completed(succeeded or failed or canceled).
     * The callback will be execute asynchronously on background thread.
     *
     * @param thenCallback the callback when this promise is completed
     * @param <TNextOut> the type of callback output value
     * @return promise to return a result value
     */
    <TNextOut> Promise<TNextOut> then(ThenCallback<TOut, TNextOut> thenCallback);

    /**
     * Sets the callback when this promise is completed(succeeded or failed or canceled).
     * The callback will be execute asynchronously on background thread.
     * Use to watch a result. The callback can not exchange result value to other value.
     *
     * @param watchCallback the callback when this promise is completed
     * @return promise to return a result value
     */
    Promise<TOut> watch(WatchCallback<TOut> watchCallback);

    /**
     * Sets the callback when this promise is succeeded.
     * The callback will be execute asynchronously on background thread.
     *
     * @param successCallback the callback when this promise is succeeded.
     * @param <TNextOut> the type of the callback output value
     * @return promise to return a result value
     */
    <TNextOut> Promise<TNextOut> succeeded(SuccessCallback<TOut, TNextOut> successCallback);

    /**
     * Sets the callback when this promise is failed.
     * The callback will be execute asynchronously on background thread.
     *
     * @param failCallback the callback when this promise is failed.
     * @return promise to return a result value
     */
    Promise<TOut> failed(FailCallback<TOut> failCallback);

    /**
     * Sets the callback when this promise is completed(succeeded or failed or canceled).
     * The callback will be execute asynchronously on background thread.
     * This is the end point of callback chain.
     *
     * @param finishCallback the callback when this promise is completed.
     * @return the object thatF can submit a task for promise execution
     */
    Submittable finish(FinishCallback<TOut> finishCallback);

    /**
     * Exchanges this promise result to other value, if the promise is succeeded.
     *
     * @param replace the value to exchange
     * @param <TReplace> the specific type of replacing value
     * @return promise to return the exchanged result
     */
    <TReplace> Promise<TReplace> exchange(TReplace replace);
}
