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

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class PromisesTest {

    @Test
    public void testWhen() throws Exception {
        final Deferrable<String> deferrable = new Deferrable<>();

        Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                return Defer.success("Hello Promise");
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(FinishParams<String> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit();

        assertEquals("Hello Promise", deferrable.getResult().safeGetValue());
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Test
    public void testThrowException() throws Exception {
        final Deferrable<String> deferrable = new Deferrable<>();

        Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                throw new Exception("Hello Promise");
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(FinishParams<String> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit();

        assertEquals("Hello Promise", deferrable.getResult().getException().getMessage());
    }

    @Test
    public void testCancellableWhenSleep() throws Exception {

        final Deferrable<String> deferrable = new Deferrable<>();

        final CountDownLatch latch = new CountDownLatch(1);

        final Canceller canceller = Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                latch.countDown();
                TimeUnit.NANOSECONDS.sleep(Long.MAX_VALUE);
                throw new UnsupportedOperationException();
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(FinishParams<String> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit();

        latch.await();

        canceller.cancel();

        assertTrue(deferrable.getResult().getCancelToken().isCanceled());
    }

    @Test
    public void testCancellableWhenNotRunningFuture() throws Exception {
        final Deferrable<String> deferrable = new Deferrable<>();

        final ExecutorService executor = Executors.newSingleThreadExecutor();

        final CountDownLatch latch = new CountDownLatch(1);

        Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                latch.await();
                return Defer.success("Hello Promise");
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(FinishParams<String> params) {
                deferrable.setResult(params.asResult());
            }
        }).submitOn(executor);

        final Canceller canceller = Promises.when(new WhenCallback<String>() {

            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                throw new UnsupportedOperationException();
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(FinishParams<String> params) {
                if (params.getCancelToken().isCanceled()) {
                    latch.countDown();
                }
            }
        }).submitOn(executor);

        canceller.cancel();

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        assertEquals("Hello Promise", deferrable.getResult().safeGetValue());
    }

    @Test
    public void testCallSucceeded() throws Exception {
        final Deferrable<String> deferrable = new Deferrable<>();

        Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                return Defer.success("Hello");
            }
        }).succeeded(new SuccessCallback<String, String>() {
            @Override
            public Deferred<String> run(SuccessParams<String> params) throws Exception {
                return Defer.success(params.getValue() + " Promise");
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(FinishParams<String> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit();

        assertEquals("Hello Promise", deferrable.getResult().safeGetValue());
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Test
    public void testNotCallSucceededWhenFailed() throws InterruptedException {
        final Deferrable<String> deferrable = new Deferrable<>();

        Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                throw new Exception("Hello Promise");
            }
        }).succeeded(new SuccessCallback<String, String>() {
            @Override
            public Deferred<String> run(SuccessParams<String> params) throws Exception {
                throw new UnsupportedOperationException();
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(FinishParams<String> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit();

        assertEquals("Hello Promise", deferrable.getResult().getException().getMessage());
    }

    @Test
    public void testNotCallSucceededWhenCanceled() throws InterruptedException {
        final Deferrable<String> deferrable = new Deferrable<>();

        Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                TimeUnit.NANOSECONDS.sleep(Long.MAX_VALUE);
                throw new UnsupportedOperationException();
            }
        }).succeeded(new SuccessCallback<String, String>() {
            @Override
            public Deferred<String> run(SuccessParams<String> params) throws Exception {
                throw new UnsupportedOperationException();
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(FinishParams<String> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit().cancel();

        assertTrue(deferrable.getResult().getCancelToken().isCanceled());
        assertNull(deferrable.getResult().getException());
    }

    @Test
    public void testCallFailed() throws Exception {
        final Deferrable<String> deferrable = new Deferrable<>();

        Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                throw new Exception();
            }
        }).failed(new FailCallback<String>() {
            @Override
            public Deferred<String> run(FailParams<String> params) throws Exception {
                return Defer.success("Hello Promise");
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(FinishParams<String> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit();

        assertEquals("Hello Promise", deferrable.getResult().safeGetValue());
    }

    @Test
    public void testNotCallFailedWhenSucceeded() throws Exception {
        final Deferrable<String> deferrable = new Deferrable<>();

        Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                return Defer.success("Hello Promise");
            }
        }).failed(new FailCallback<String>() {
            @Override
            public Deferred<String> run(FailParams<String> params) throws Exception {
                throw new UnsupportedOperationException();
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(FinishParams<String> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit();

        assertEquals("Hello Promise", deferrable.getResult().safeGetValue());
    }

    @Test
    public void testNotCallFailedWhenCanceled() throws Exception {
        final Deferrable<String> deferrable = new Deferrable<>();

        Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                TimeUnit.NANOSECONDS.sleep(Long.MAX_VALUE);
                throw new UnsupportedOperationException();
            }
        }).failed(new FailCallback<String>() {
            @Override
            public Deferred<String> run(FailParams<String> params) throws Exception {
                throw new UnsupportedOperationException();
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(FinishParams<String> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit().cancel();

        assertTrue(deferrable.getResult().getCancelToken().isCanceled());
        assertNull(deferrable.getResult().getException());
    }

    @Test
    public void testCallWatchWhenSucceeded() throws Exception {

        final AtomicInteger count = new AtomicInteger();

        final Deferrable<String> deferrable = new Deferrable<>();

        Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                return Defer.success("Hello Promise");
            }
        }).watch(new WatchCallback<String>() {
            @Override
            public void run(WatchParams<String> params) throws Exception {
                if ("Hello Promise".equals(params.safeGetValue())) {
                    count.incrementAndGet();
                }
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(FinishParams<String> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit();

        assertEquals("Hello Promise", deferrable.getResult().safeGetValue());

        assertEquals(1, count.get());
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Test
    public void testCallWatchWhenFailed() throws InterruptedException {

        final AtomicInteger count = new AtomicInteger();

        final Deferrable<String> deferrable = new Deferrable<>();

        Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                throw new Exception("Hello Promise");
            }
        }).watch(new WatchCallback<String>() {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
            @Override
            public void run(WatchParams<String> params) throws Exception {
                if ("Hello Promise".equals(params.getException().getMessage())) {
                    count.incrementAndGet();
                }
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(FinishParams<String> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit();

        assertEquals("Hello Promise", deferrable.getResult().getException().getMessage());

        assertEquals(1, count.get());
    }

    @Test
    public void testCallWatchWhenCanceled() throws InterruptedException {

        final AtomicInteger count = new AtomicInteger();

        final Deferrable<String> deferrable = new Deferrable<>();

        Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                TimeUnit.NANOSECONDS.sleep(Long.MAX_VALUE);
                throw new UnsupportedOperationException();
            }
        }).watch(new WatchCallback<String>() {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
            @Override
            public void run(WatchParams<String> params) throws Exception {
                if (params.getCancelToken().isCanceled()) {
                    count.incrementAndGet();
                }
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(FinishParams<String> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit().cancel();

        assertTrue(deferrable.getResult().getCancelToken().isCanceled());
        assertNull(deferrable.getResult().getException());

        assertEquals(1, count.get());
    }

    @Test
    public void testThenSucceeded() throws Exception {
        final Deferrable<String> deferrable = new Deferrable<>();

        Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                return Defer.success("Hello");
            }
        }).then(new ThenCallback<String, String>() {
            @Override
            public Deferred<String> run(ThenParams<String> params) throws Exception {
                final String value = params.safeGetValue();
                return Defer.success(value + " Promise");
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(FinishParams<String> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit();

        assertEquals("Hello Promise", deferrable.getResult().safeGetValue());
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Test
    public void testThenFailed() {
        final Deferrable<String> deferrable = new Deferrable<>();

        Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                throw new Exception("Hello");
            }
        }).then(new ThenCallback<String, String>() {
            @Override
            public Deferred<String> run(ThenParams<String> params) throws Exception {
                if (params.getCancelToken().isCanceled()) {
                    throw new UnsupportedOperationException();
                }

                final Exception e = params.getException();
                if (e == null) {
                    throw new UnsupportedOperationException();
                }
                throw new Exception(e.getMessage() + " Promise", e);
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(FinishParams<String> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit();

        assertEquals("Hello Promise", deferrable.getResult().getException().getMessage());
    }

    @Test
    public void testThenCanceled() {
        final Deferrable<String> deferrable = new Deferrable<>();
        final AtomicInteger count = new AtomicInteger();

        Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                TimeUnit.NANOSECONDS.sleep(Long.MAX_VALUE);
                throw new UnsupportedOperationException();
            }
        }).then(new ThenCallback<String, String>() {
            @Override
            public Deferred<String> run(ThenParams<String> params) throws Exception {
                if (params.getCancelToken().isCanceled()) {
                    count.incrementAndGet();
                    return Defer.cancel();
                }
                throw new UnsupportedOperationException();
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(FinishParams<String> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit().cancel();

        assertTrue(deferrable.getResult().getCancelToken().isCanceled());
        assertNull(deferrable.getResult().getException());
    }

    @Test
    public void testCallPromiseOnPromise() throws Exception {

        final Deferrable<String> deferrable = new Deferrable<>();

        Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                return Promises.when(new WhenCallback<String>() {
                    @Override
                    public Deferred<String> run(WhenParams params) throws Exception {
                        return Defer.success("Hello Promise");
                    }
                }).get(params);
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(FinishParams<String> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit();

        assertEquals("Hello Promise", deferrable.getResult().safeGetValue());
    }

    @Test
    public void testPromiseGetOn() throws Exception {

        final Deferrable<String> deferrable = new Deferrable<>();

        final ExecutorService executor = Executors.newSingleThreadExecutor();

        Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                final long threadId = Thread.currentThread().getId();
                return Promises.when(new WhenCallback<String>() {
                    @Override
                    public Deferred<String> run(WhenParams params) throws Exception {
                        if (threadId == Thread.currentThread().getId()) {
                            throw new UnsupportedOperationException();
                        }
                        return Defer.success("Hello Promise");
                    }
                }).getOn(executor, params);
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(FinishParams<String> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit();

        assertEquals("Hello Promise", deferrable.getResult().safeGetValue());

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    @Test
    public void testPromiseOn() throws Exception {

        final Deferrable<String> deferrable = new Deferrable<>();

        final ExecutorService executor = Executors.newSingleThreadExecutor();

        Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                final long threadId = Thread.currentThread().getId();
                return Promises.when(new WhenCallback<String>() {
                    @Override
                    public Deferred<String> run(WhenParams params) throws Exception {
                        if (threadId == Thread.currentThread().getId()) {
                            throw new UnsupportedOperationException();
                        }
                        return Defer.success("Hello Promise");
                    }
                }).promiseOn(executor).get(params);
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(FinishParams<String> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit();

        assertEquals("Hello Promise", deferrable.getResult().safeGetValue());

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    @Test
    public void testRepeat() throws Exception {

        final Deferrable<Integer> deferrable = new Deferrable<>();

        Promises.repeat(new RepeatCallback<Integer>() {
            @Override
            public Deferred<Integer> run(RepeatParams params) throws Exception {
                final int index = params.getIndex().get();
                return Defer.success(index);
            }
        }).until(new UntilCallback<Integer>() {
            @Override
            public boolean run(UntilParams<Integer> params) throws Exception {
                final int index = params.getIndex().incrementAndGet();
                return index >= 3;
            }
        }).finish(new FinishCallback<Integer>() {
            @Override
            public void run(FinishParams<Integer> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit();

        assertEquals(2, deferrable.getResult().safeGetValue().intValue());
    }

    @Test
    public void testForEach() throws Exception {

        final Deferrable<AtomicInteger> deferrable = new Deferrable<>();

        final List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);

        Promises.forEach(list, new AtomicInteger(), new ForEachCallback<Integer, AtomicInteger>() {
            @Override
            public Deferred<ForEachOp> run(ForEachParams<Integer, AtomicInteger> params) throws Exception {
                params.getOperand().addAndGet(params.getValue());
                return Defer.success(ForEachOp.CONTINUE);
            }
        }).finish(new FinishCallback<AtomicInteger>() {
            @Override
            public void run(FinishParams<AtomicInteger> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit();

        assertEquals(1 + 2 + 3, deferrable.getResult().safeGetValue().get());
    }

    @Test
    public void testForEachBreak() throws Exception {

        final Deferrable<AtomicInteger> deferrable = new Deferrable<>();

        final List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);

        Promises.forEach(list, new AtomicInteger(), new ForEachCallback<Integer, AtomicInteger>() {
            @Override
            public Deferred<ForEachOp> run(ForEachParams<Integer, AtomicInteger> params) throws Exception {
                if (params.getIndex().getAndIncrement() == 2) {
                    return Defer.success(ForEachOp.BREAK);
                }
                params.getOperand().addAndGet(params.getValue());
                return Defer.success(ForEachOp.CONTINUE);
            }
        }).finish(new FinishCallback<AtomicInteger>() {
            @Override
            public void run(FinishParams<AtomicInteger> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit();

        assertEquals(1 + 2, deferrable.getResult().safeGetValue().get());
    }

    @Test
    public void testExchange() throws Exception {
        final Deferrable<String> deferrable = new Deferrable<>();

        Promises.when(new WhenCallback<Integer>() {
            @Override
            public Deferred<Integer> run(WhenParams params) throws Exception {
                return Defer.success(0);
            }
        }).exchange("Hello Promise").finish(new FinishCallback<String>() {
            @Override
            public void run(FinishParams<String> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit();

        assertEquals("Hello Promise", deferrable.getResult().safeGetValue());
    }

    @Test
    public void testSchedule() throws Exception {
        final Deferrable<String> deferrable = new Deferrable<>();

        final Scheduler scheduler = new Scheduler(1);

        Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                return Defer.success("Hello Promise");
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(FinishParams<String> params) {
                deferrable.setResult(params.asResult());
            }
        }).submitOn(scheduler);

        assertEquals("Hello Promise", deferrable.getResult().safeGetValue());
    }

    @Test
    public void testSerialExecuting() throws Exception {

        final Scheduler scheduler = new Scheduler(1);

        final ManualResetEvent event1 = new ManualResetEvent(false);
        final Deferrable<String> deferrable1 = new Deferrable<>();
        Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                event1.await();
                return Defer.success("Hello Promise 1");
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(FinishParams<String> params) {
                deferrable1.setResult(params.asResult());
            }
        }).submitOn(scheduler);

        final ManualResetEvent event2 = new ManualResetEvent(false);
        final Deferrable<String> deferrable2 = new Deferrable<>();
        Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                event2.set();
                return Defer.success("Hello Promise 2");
            }
        }).finish(new FinishCallback<String>() {
            @Override
            public void run(FinishParams<String> params) {
                deferrable2.setResult(params.asResult());
            }
        }).submitOn(scheduler);

        assertFalse(event2.await(5, TimeUnit.SECONDS));

        event1.set();

        event2.await();

        assertEquals("Hello Promise 1", deferrable1.getResult().safeGetValue());
        assertEquals("Hello Promise 2", deferrable2.getResult().safeGetValue());
    }

    @Test
    public void testGetOnScheduler() throws Exception {
        final Scheduler scheduler = new Scheduler(1);
        final Deferrable<Integer> deferrable = new Deferrable<>();
        Promises.when(new WhenCallback<Integer>() {
            @Override
            public Deferred<Integer> run(WhenParams params) throws Exception {
                return Promises.when(new WhenCallback<Integer>() {
                    @Override
                    public Deferred<Integer> run(WhenParams params) throws Exception {
                        return Defer.success(616);
                    }
                }).getOn(scheduler, params);
            }
        }).finish(new FinishCallback<Integer>() {
            @Override
            public void run(FinishParams<Integer> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit();

        assertEquals(616, deferrable.getResult().safeGetValue().intValue());
    }

    @Test
    public void testPromiseOnScheduler() throws Exception {
        final Scheduler scheduler = new Scheduler(1);
        final Deferrable<Integer> deferrable = new Deferrable<>();
        Promises.when(new WhenCallback<Integer>() {
            @Override
            public Deferred<Integer> run(WhenParams params) throws Exception {
                return Promises.when(new WhenCallback<Integer>() {
                    @Override
                    public Deferred<Integer> run(WhenParams params) throws Exception {
                        return Defer.success(616);
                    }
                }).promiseOn(scheduler).get(params);
            }
        }).finish(new FinishCallback<Integer>() {
            @Override
            public void run(FinishParams<Integer> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit();

        assertEquals(616, deferrable.getResult().safeGetValue().intValue());
    }

    @Test
    public void testWhenAll() throws Exception {

        final Deferrable<List<String>> deferrable = new Deferrable<>();

        Promises.whenAll(Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                return Defer.success("A");
            }
        }), Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                return Defer.success("B");
            }
        })).finish(new FinishCallback<List<String>>() {
            @Override
            public void run(FinishParams<List<String>> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit();

        final List<String> results = deferrable.getResult().safeGetValue();

        assertEquals("A", results.get(0));
        assertEquals("B", results.get(1));
    }

    @Test
    public void testWhenAny() throws Exception {
        final ManualResetEvent noSignal = new ManualResetEvent(false);
        final ManualResetEvent aborted = new ManualResetEvent(false);
        final Deferrable<String> deferrable = new Deferrable<>();
        Promises.whenAny(Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                return Defer.success("A");
            }
        }), Promises.when(new WhenCallback<String>() {
            @Override
            public Deferred<String> run(WhenParams params) throws Exception {
                noSignal.await();
                return Defer.success("B");
            }
        }).watch(new WatchCallback<String>() {
            @Override
            public void run(WatchParams<String> params) throws Exception {
                aborted.set();
            }
        })).finish(new FinishCallback<String>() {
            @Override
            public void run(FinishParams<String> params) {
                deferrable.setResult(params.asResult());
            }
        }).submit();

        final String value = deferrable.getResult().safeGetValue();
        assertEquals("A", value);

        assertTrue(aborted.await(20, TimeUnit.SECONDS));

    }
}