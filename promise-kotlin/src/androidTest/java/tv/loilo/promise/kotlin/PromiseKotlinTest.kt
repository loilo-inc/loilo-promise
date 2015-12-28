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

package tv.loilo.promise.kotlin

import android.test.InstrumentationTestCase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by pepeotoito on 2015/12/27.
 */
class PromiseKotlinTest : InstrumentationTestCase() {

    fun withOneshotExecutor(f: (ExecutorService) -> Unit) {
        val executor = Executors.newSingleThreadExecutor()

        f(executor)

        executor.shutdown()
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
    }

    fun testWhen() {

        val count = AtomicInteger()

        withOneshotExecutor {
            promiseWhen {
                defer {
                    count.incrementAndGet()
                }
            }.submitOn(it)
        }

        assertEquals(1, count.get())
    }

    fun testThenSucceeded() {

        withOneshotExecutor {
            promiseWhen {
                defer {

                }
            }.then {
                defer {

                }
            }.submitOn(it)
        }
    }
}
