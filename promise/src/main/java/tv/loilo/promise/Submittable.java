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

import java.util.concurrent.ExecutorService;

/**
 * Interface to submit a task for execution.
 */
public interface Submittable {

    /**
     * Submits a task of this object for execution and returns a task cancellation handle.
     *
     * @param executorService the executor to submit this task
     * @param tag             the tag associated with this execution (the tag can access from callback parameters)
     * @return submitted task cancellation handle
     */
    Canceller submitOn(ExecutorService executorService, Object tag);


    /**
     * Submits a task of this object for execution and returns a task cancellation handle.
     * @param executorService the executor to submit this task
     * @return submitted task cancellation handle
     */
    Canceller submitOn(ExecutorService executorService);

    /**
     * Submits a task of this object for execution and returns a task cancellation handle.
     * The task will running on default ExecutorService.
     * @param tag the tag associated with this execution (the tag can access from callback parameters)
     * @return submitted task cancellation handle
     */
    Canceller submit(Object tag);

    /**
     * Submits a task of this object for execution and returns a task cancellation handle.
     * The task will running on default ExecutorService.
     * @return submitted task cancellation handle
     */
    Canceller submit();

    /**
     * Submits a task of this object for execution and returns a task cancellation handle.
     * The task is scheduled by Scheduler and the task will running on default ExecutorService.
     *
     * @param scheduler the execution scheduler.
     * @param tag       the tag associated with this execution (the tag can access from callback parameters)
     * @return submitted task cancellation handle
     */
    Canceller submitOn(Scheduler scheduler, Object tag);


    /**
     * Submits a task of this object for execution and returns a task cancellation handle.
     * The task is scheduled by Scheduler and the task will running on default ExecutorService.
     *
     * @param scheduler the execution scheduler.
     * @return submitted task cancellation handle
     */
    Canceller submitOn(Scheduler scheduler);
}
