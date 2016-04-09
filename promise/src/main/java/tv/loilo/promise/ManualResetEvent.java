package tv.loilo.promise;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ManualResetEvent {

    private AtomicReference<CountDownLatch> mLatch;

    public ManualResetEvent(boolean initialState) {

        if (initialState) {
            mLatch = new AtomicReference<>();
        } else {
            mLatch = new AtomicReference<>(createOneCountDownLatch());
        }
    }

    private static CountDownLatch createOneCountDownLatch() {
        return new CountDownLatch(1);
    }

    public boolean set() {
        final CountDownLatch latch = mLatch.getAndSet(null);
        if (latch == null) {
            return false;
        }
        latch.countDown();
        return true;
    }

    public boolean reset() {
        return mLatch.compareAndSet(null, createOneCountDownLatch());
    }

    public boolean await(long timeout, TimeUnit timeUnit) throws InterruptedException {
        final CountDownLatch latch = mLatch.get();
        return latch == null || latch.await(timeout, timeUnit);
    }

    public void await() throws InterruptedException {
        final CountDownLatch latch = mLatch.get();
        if (latch == null) {
            return;
        }

        latch.await();
    }

    public boolean isSignaled() {
        final CountDownLatch latch = mLatch.get();
        return latch == null;
    }
}
