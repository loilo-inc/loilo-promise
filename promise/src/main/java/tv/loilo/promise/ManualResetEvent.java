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
