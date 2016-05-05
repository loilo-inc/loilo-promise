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
import java.util.concurrent.TimeUnit;

public final class AutoResetEvent {
    private static final Object ENTRY = new Object();

    private final ArrayBlockingQueue<Object> mQueue;

    public AutoResetEvent() {
        mQueue = new ArrayBlockingQueue<>(1);
    }

    public void set() {
        mQueue.offer(ENTRY);
    }

    public void reset() {
        mQueue.clear();
    }

    public boolean await(long timeout, TimeUnit timeUnit) throws InterruptedException {
        final Object value = mQueue.poll(timeout, timeUnit);
        return value != null;
    }

    public void await() throws InterruptedException {
        mQueue.take();
    }

    public boolean isSignaled() {
        return mQueue.size() > 0;
    }
}
