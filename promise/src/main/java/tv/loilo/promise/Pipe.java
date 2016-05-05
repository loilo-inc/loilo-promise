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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class for transferring the data only once between different threads.
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
