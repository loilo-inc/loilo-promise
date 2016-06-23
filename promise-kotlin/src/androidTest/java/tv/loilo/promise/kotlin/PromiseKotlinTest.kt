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

package tv.loilo.promise.kotlin

import android.support.test.runner.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import tv.loilo.promise.*
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import junit.framework.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class PromiseKotlinTest {

    @Test
    fun testWhen() {
        val deferrable = Deferrable<String>()

        promiseWhen {
            defer { "Hello Promise" }
        }.finish {
            deferrable.result = it.asResult()
        }.submit()

        assertEquals("Hello Promise", deferrable.result.safeGetValue())
    }

    @Test
    fun testThrowException() {
        val deferrable = Deferrable<Unit>()

        promiseWhen {
            defer<Unit> { throw Exception("Hello Promise") }
        }.finish {
            deferrable.result = it.asResult()
        }.submit()

        assertEquals("Hello Promise", deferrable.result.exception.message)
    }

    @Test
    fun testCancellableWhenSleep() {

        val deferrable = Deferrable<Unit>()

        val latch = CountDownLatch(1)

        val canceller = promiseWhen {
            defer<Unit> {
                latch.countDown()
                TimeUnit.NANOSECONDS.sleep(Long.MAX_VALUE)
                throw UnsupportedOperationException();
            }
        }.finish {
            deferrable.result = it.asResult()
        }.submit()

        latch.await()

        canceller.cancel()

        assertTrue(deferrable.result.cancelToken.isCanceled)
    }

    @Test
    fun testCancellableWhenNotRunningFuture() {
        val deferrable = Deferrable<String>()

        val executor = Executors.newSingleThreadExecutor()

        val latch = CountDownLatch(1)

        promiseWhen {
            defer {
                latch.await()
                "Hello Promise"
            }
        }.finish {
            deferrable.result = it.asResult()
        }.submitOn(executor)

        promiseWhen {
            defer<Unit> { throw UnsupportedOperationException() }
        }.finish {
            if (it.cancelToken.isCanceled) {
                latch.countDown()
            }
        }.submitOn(executor).cancel()

        executor.shutdown()
        executor.awaitTermination(java.lang.Long.MAX_VALUE, TimeUnit.NANOSECONDS)

        assertEquals("Hello Promise", deferrable.result.safeGetValue())
    }

    @Test
    fun testCallSucceeded() {
        val deferrable = Deferrable<String>()

        promiseWhen {
            defer { "Hello" }
        }.succeeded {
            defer { "${it.value} Promise" }
        }.finish {
            deferrable.result = it.asResult()
        }.submit()

        assertEquals("Hello Promise", deferrable.result.safeGetValue())
    }

    @Test
    fun testNotCallSucceededWhenFailed() {
        val deferrable = Deferrable<Unit>()

        promiseWhen {
            defer<Unit> { throw Exception("Hello Promise") }
        }.succeeded {
            defer<Unit> { throw UnsupportedOperationException() }
        }.finish {
            deferrable.result = it.asResult()
        }.submit()

        assertEquals("Hello Promise", deferrable.result.exception.message)
    }

    @Test
    fun testNotCallSucceededWhenCanceled() {
        val deferrable = Deferrable<Unit>()

        promiseWhen {
            defer<Unit> {
                TimeUnit.NANOSECONDS.sleep(Long.MAX_VALUE)
                throw UnsupportedOperationException()
            }
        }.succeeded {
            defer<Unit> { throw UnsupportedOperationException() }
        }.finish {
            deferrable.result = it.asResult()
        }.submit().cancel()

        assertTrue(deferrable.result.cancelToken.isCanceled)
        assertNull(deferrable.result.exception)
    }

    @Test
    fun testCallFailed() {
        val deferrable = Deferrable<String>()

        promiseWhen {
            defer<String> { throw Exception() }
        }.failed {
            defer { "Hello Promise" }
        }.finish {
            deferrable.result = it.asResult()
        }.submit()

        assertEquals("Hello Promise", deferrable.result.safeGetValue())
    }

    @Test
    fun testNotCallFailedWhenSucceeded() {
        val deferrable = Deferrable<String>()

        promiseWhen {
            defer { "Hello Promise" }
        }.failed {
            defer { throw UnsupportedOperationException() }
        }.finish {
            deferrable.result = it.asResult()
        }.submit()

        assertEquals("Hello Promise", deferrable.result.safeGetValue())
    }

    @Test
    fun testNotCallFailedWhenCanceled() {
        val deferrable = Deferrable<Unit>()

        promiseWhen {
            defer<Unit> {
                TimeUnit.NANOSECONDS.sleep(Long.MAX_VALUE)
                throw UnsupportedOperationException()
            }
        }.failed {
            defer<Unit> { throw UnsupportedOperationException() }
        }.finish {
            deferrable.result = it.asResult()
        }.submit().cancel()

        assertTrue(deferrable.result.cancelToken.isCanceled)
        assertNull(deferrable.result.exception)
    }

    @Test
    fun testCallWatchWhenSucceeded() {

        val count = AtomicInteger()

        val deferrable = Deferrable<String>()

        promiseWhen {
            defer { "Hello Promise" }
        }.watch {
            if ("Hello Promise" == it.safeGetValue()) {
                count.incrementAndGet()
            }
        }.finish {
            deferrable.result = it.asResult()
        }.submit()

        assertEquals("Hello Promise", deferrable.result.safeGetValue())

        assertEquals(1, count.get())
    }

    @Test
    fun testCallWatchWhenFailed() {

        val count = AtomicInteger()

        val deferrable = Deferrable<Unit>()

        promiseWhen {
            defer<Unit> { throw Exception("Hello Promise") }
        }.watch {
            if ("Hello Promise" == it.exception.message) {
                count.incrementAndGet()
            }
        }.finish {
            deferrable.result = it.asResult()
        }.submit()

        assertEquals("Hello Promise", deferrable.result.exception.message)

        assertEquals(1, count.get())
    }

    @Test
    fun testCallWatchWhenCanceled() {

        val count = AtomicInteger()

        val deferrable = Deferrable<Unit>()

        promiseWhen {
            defer<Unit> {
                TimeUnit.NANOSECONDS.sleep(Long.MAX_VALUE)
                throw UnsupportedOperationException()
            }
        }.watch {
            if (it.cancelToken.isCanceled) {
                count.incrementAndGet()
            }
        }.finish {
            deferrable.result = it.asResult()
        }.submit().cancel()

        assertTrue(deferrable.result.cancelToken.isCanceled)
        assertNull(deferrable.result.exception)

        assertEquals(1, count.get())
    }

    @Test
    fun testThenSucceeded() {
        val deferrable = Deferrable<String>()

        promiseWhen {
            defer { "Hello" }
        }.then {
            defer { "${it.safeGetValue()} Promise" }
        }.finish {
            deferrable.result = it.asResult()
        }.submit()

        assertEquals("Hello Promise", deferrable.result.safeGetValue())
    }

    @Test
    fun testThenFailed() {
        val deferrable = Deferrable<Unit>()

        promiseWhen {
            defer<Unit> { throw Exception("Hello") }
        }.then {
            defer<Unit> {
                it.whenFailed {
                    throw Exception("${it.message} Promise", it)
                }
                throw UnsupportedOperationException()
            }
        }.finish {
            deferrable.result = it.asResult()
        }.submit()

        assertEquals("Hello Promise", deferrable.result.exception.message)
    }

    @Test
    fun testThenCanceled() {
        val deferrable = Deferrable<Unit>()
        val count = AtomicInteger()

        promiseWhen {
            defer {
                TimeUnit.NANOSECONDS.sleep(Long.MAX_VALUE)
                throw UnsupportedOperationException()
            }
        }.then {
            if (it.cancelToken.isCanceled) {
                count.incrementAndGet()
                deferCancel<Unit>()
            } else {
                throw UnsupportedOperationException()
            }
        }.finish {
            deferrable.result = it.asResult()
        }.submit().cancel()

        assertTrue(deferrable.result.cancelToken.isCanceled)
        assertNull(deferrable.result.exception)
    }

    @Test
    fun testCallPromiseOnPromise() {

        val deferrable = Deferrable<String>()

        promiseWhen {
            promiseWhen {
                defer { "Hello Promise" }
            }.get(it)
        }.finish {
            deferrable.result = it.asResult()
        }.submit()

        assertEquals("Hello Promise", deferrable.result.safeGetValue())
    }

    @Test
    fun testPromiseGetOn() {

        val deferrable = Deferrable<String>()

        val executor = Executors.newSingleThreadExecutor()

        promiseWhen {
            val threadId = Thread.currentThread().id
            promiseWhen {
                defer {
                    if (threadId == Thread.currentThread().id) {
                        throw UnsupportedOperationException()
                    }
                    "Hello Promise"
                }
            }.getOn(executor, it)
        }.finish {
            deferrable.result = it.asResult()
        }.submit()

        assertEquals("Hello Promise", deferrable.result.safeGetValue())

        executor.shutdown()
        executor.awaitTermination(java.lang.Long.MAX_VALUE, TimeUnit.NANOSECONDS)
    }

    @Test
    fun testPromiseOn() {

        val deferrable = Deferrable<String>()

        val executor = Executors.newSingleThreadExecutor()

        promiseWhen {
            val threadId = Thread.currentThread().id
            promiseWhen {
                defer {
                    if (threadId == Thread.currentThread().id) {
                        throw UnsupportedOperationException()
                    }
                    "Hello Promise"
                }
            }.promiseOn(executor).get(it)
        }.finish {
            deferrable.result = it.asResult()
        }.submit()

        assertEquals("Hello Promise", deferrable.result.safeGetValue())

        executor.shutdown()
        executor.awaitTermination(java.lang.Long.MAX_VALUE, TimeUnit.NANOSECONDS)
    }

    @Test
    fun testRepeat() {

        val deferrable = Deferrable<Int>()

        promiseRepeat {
            defer {
                it.index.get()
            }
        }.until {
            it.index.incrementAndGet() >= 3
        }.finish {
            deferrable.result = it.asResult()
        }.submit()

        assertEquals(2, deferrable.result.safeGetValue().toInt())
    }

    @Test
    fun testForEach() {

        val deferrable = Deferrable<AtomicInteger>()

        val list = ArrayList<Int>()
        list.add(1)
        list.add(2)
        list.add(3)

        promiseForEach(list, AtomicInteger(), {
            defer {
                it.operand.addAndGet(it.value)
                ForEachOp.CONTINUE
            }
        }).finish {
            deferrable.result = it.asResult()
        }.submit()

        assertEquals(1 + 2 + 3, deferrable.result.safeGetValue().get())
    }

    @Test
    fun testForEachBreak() {

        val deferrable = Deferrable<AtomicInteger>()

        val list = ArrayList<Int>()
        list.add(1)
        list.add(2)
        list.add(3)

        promiseForEach(list, AtomicInteger(), {
            defer {
                if (it.index.andIncrement == 2) {
                    ForEachOp.BREAK
                } else {
                    it.operand.addAndGet(it.value)
                    ForEachOp.CONTINUE
                }
            }
        }).finish {
            deferrable.result = it.asResult()
        }.submit()

        assertEquals(1 + 2, deferrable.result.safeGetValue().get())
    }

    @Test
    fun testForEachWithoutOperand() {
        val deferrable = Deferrable<Unit>()

        val list = ArrayList<Int>()
        list.add(1)
        list.add(2)
        list.add(3)

        val sum = AtomicInteger()

        promiseForEach(list, {
            defer {
                if (it.index.andIncrement == 2) {
                    ForEachOp.BREAK
                } else {
                    sum.addAndGet(it.value)
                    ForEachOp.CONTINUE
                }
            }
        }).finish {
            deferrable.result = it.asResult()
        }.submit()

        deferrable.result.safeGetValue()

        assertEquals(1 + 2, sum.get())
    }

    @Test
    fun testPromiseSuccess() {
        val deferrable = Deferrable<Boolean>()
        promiseSuccess(true).finish {
            deferrable.result = it.asResult()
        }.submit()

        assertTrue(deferrable.result.safeGetValue())
    }

    @Test
    fun testPromiseFail() {
        val deferrable = Deferrable<Unit>()
        promiseFail<Unit>(Exception("Hello Promise")).finish {
            deferrable.result = it.asResult()
        }.submit()

        assertEquals("Hello Promise", deferrable.result.exception.message)
    }

    @Test
    fun testPromiseCancel() {
        val deferrable = Deferrable<Unit>()
        promiseCancel<Unit>().finish {
            deferrable.result = it.asResult()
        }.submit()

        assertTrue(deferrable.result.cancelToken.isCanceled)
        assertNull(deferrable.result.exception)
    }

    @Test
    fun testPromiseNotImpl() {
        val deferrable = Deferrable<Unit>()
        promiseNotImpl<Unit>().finish {
            deferrable.result = it.asResult()
        }.submit()

        assertTrue(deferrable.result.exception is UnsupportedOperationException)
        assertEquals("Not implemented.", deferrable.result.exception.message)
    }

    @Test
    fun testDeferSuccess() {
        assertTrue(deferSuccess(true).result.safeGetValue())
    }

    @Test
    fun testDeferFail() {
        assertEquals("Hello Promise", deferFail<Unit>(Exception("Hello Promise")).result.exception.message)
    }

    @Test
    fun testDeferCancel() {
        val deferred = deferCancel<Unit>()
        assertTrue(deferred.result.cancelToken.isCanceled)
        assertNull(deferred.result.exception)
    }

    @Test
    fun testDeferNotImpl() {
        val deferred = deferNotImpl<Unit>()
        assertTrue(deferred.result.exception is UnsupportedOperationException)
        assertEquals("Not implemented.", deferred.result.exception.message)
    }

    @Test
    fun testDeferrableSuccess() {
        val deferred = deferrable<Boolean> {
            it.setSucceeded(true)
        }
        assertTrue(deferred.result.safeGetValue())
    }

    @Test
    fun testDeferrableFail() {
        val deferred = deferrable<Unit> {
            it.setFailed(Exception("Hello Promise"))
        }

        assertEquals("Hello Promise", deferred.result.exception.message)
    }

    @Test
    fun testDeferrableCanceled() {
        val deferred = deferrable<Unit> {
            it.setCanceled()
        }

        assertTrue(deferred.result.cancelToken.isCanceled)
        assertNull(deferred.result.exception)
    }

    @Test
    fun testResultWhenSucceeded() {
        Results.success(true).whenSucceeded {
            assertTrue(it)
        } ?: throw UnsupportedOperationException()

        Results.success(true).whenSucceeded({
            assertTrue(it)
        }, whenFailed = {
            throw UnsupportedOperationException()
        }) ?: throw UnsupportedOperationException()

        Results.fail<Unit>(Exception("Hello Promise")).whenSucceeded {
            throw UnsupportedOperationException()
        }

        Results.fail<Unit>(Exception("Hello Promise")).whenSucceeded ({
            throw UnsupportedOperationException()
        }, whenFailed = {
            assertEquals("Hello Promise", it.message)
        }) ?: throw UnsupportedOperationException()

        Results.cancel<Unit>().whenSucceeded {
            throw UnsupportedOperationException()
        }

        Results.cancel<Unit>().whenSucceeded({
            throw UnsupportedOperationException()
        }, whenFailed = {
            throw UnsupportedOperationException()
        })
    }

    @Test
    fun testResultWhenFailed() {
        Results.success(true).whenFailed {
            throw UnsupportedOperationException()
        }

        Results.success(true).whenFailed({
            throw UnsupportedOperationException()
        }, whenSucceeded = {
            assertTrue(it)
        }) ?: throw UnsupportedOperationException()

        Results.fail<Unit>(Exception("Hello Promise")).whenFailed {
            assertEquals("Hello Promise", it.message)
        } ?: throw UnsupportedOperationException()

        Results.fail<Unit>(Exception("Hello Promise")).whenFailed ({
            assertEquals("Hello Promise", it.message)
        }, whenSucceeded = {
            throw UnsupportedOperationException()
        }) ?: throw UnsupportedOperationException()

        Results.cancel<Unit>().whenFailed {
            throw UnsupportedOperationException()
        }

        Results.cancel<Unit>().whenFailed({
            throw UnsupportedOperationException()
        }, whenSucceeded = {
            throw UnsupportedOperationException()
        })
    }

    @Test
    fun testResultParamsWhenSucceeded() {
        ResultParams(Results.success(true), ArrayCloseableStack(), null).whenSucceeded {
            assertTrue(it)
        } ?: throw UnsupportedOperationException()

        ResultParams(Results.success(true), ArrayCloseableStack(), null).whenSucceeded({
            assertTrue(it)
        }, whenFailed = {
            throw UnsupportedOperationException()
        }) ?: throw UnsupportedOperationException()

        ResultParams(Results.fail<Unit>(Exception("Hello Promise")), ArrayCloseableStack(), null).whenSucceeded {
            throw UnsupportedOperationException()
        }

        ResultParams(Results.fail<Unit>(Exception("Hello Promise")), ArrayCloseableStack(), null).whenSucceeded ({
            throw UnsupportedOperationException()
        }, whenFailed = {
            assertEquals("Hello Promise", it.message)
        }) ?: throw UnsupportedOperationException()

        ResultParams(Results.cancel<Unit>(), ArrayCloseableStack(), null).whenSucceeded {
            throw UnsupportedOperationException()
        }

        ResultParams(Results.cancel<Unit>(), ArrayCloseableStack(), null).whenSucceeded({
            throw UnsupportedOperationException()
        }, whenFailed = {
            throw UnsupportedOperationException()
        })
    }

    @Test
    fun testResultParamsWhenFailed() {
        ResultParams(Results.success(true), ArrayCloseableStack(), null).whenFailed {
            throw UnsupportedOperationException()
        }

        ResultParams(Results.success(true), ArrayCloseableStack(), null).whenFailed({
            throw UnsupportedOperationException()
        }, whenSucceeded = {
            assertTrue(it)
        }) ?: throw UnsupportedOperationException()

        ResultParams(Results.fail<Unit>(Exception("Hello Promise")), ArrayCloseableStack(), null).whenFailed {
            assertEquals("Hello Promise", it.message)
        } ?: throw UnsupportedOperationException()

        ResultParams(Results.fail<Unit>(Exception("Hello Promise")), ArrayCloseableStack(), null).whenFailed ({
            assertEquals("Hello Promise", it.message)
        }, whenSucceeded = {
            throw UnsupportedOperationException()
        }) ?: throw UnsupportedOperationException()

        ResultParams(Results.cancel<Unit>(), ArrayCloseableStack(), null).whenFailed {
            throw UnsupportedOperationException()
        }

        ResultParams(Results.cancel<Unit>(), ArrayCloseableStack(), null).whenFailed({
            throw UnsupportedOperationException()
        }, whenSucceeded = {
            throw UnsupportedOperationException()
        })
    }
}
