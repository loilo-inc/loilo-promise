package tv.loilo.promise;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Junpei on 2015/10/26.
 */
public final class Pipe<T> {
    private final BlockingQueue<T> mQueue;
    private final AtomicBoolean mHasValue;
    private final AtomicReference<T> mCache;
    private final Lock mGetLock;

    public Pipe() {
        mQueue = new ArrayBlockingQueue<>(1);
        mHasValue = new AtomicBoolean();
        mCache = new AtomicReference<>();
        mGetLock = new ReentrantLock();
    }

    public void set(T value) {
        if (mHasValue.getAndSet(true)) {
            return;
        }

        mQueue.offer(value);
    }

    public T get() throws InterruptedException {

        mGetLock.lockInterruptibly();
        try {
            final T cache = mCache.get();
            if (cache != null) {
                return cache;
            }

            final T item = mQueue.take();
            mCache.set(item);
            return item;

        } finally {
            mGetLock.unlock();
        }
    }
}
