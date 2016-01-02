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

import android.test.AndroidTestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PromisesTest extends AndroidTestCase {

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
}