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

import android.os.Looper;

import java.util.concurrent.atomic.AtomicReference;

public final class MessageLoop {

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final Thread mThread;
    private final Looper mLooper;

    public MessageLoop(Thread thread, Looper looper) {
        mThread = thread;
        mLooper = looper;
    }

    public Looper getLooper() {
        return mLooper;
    }

    public static MessageLoop run() {
        final ManualResetEvent event = new ManualResetEvent(false);
        final AtomicReference<Looper> looperTransit = new AtomicReference<>();
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Looper.prepare();
                    looperTransit.set(Looper.myLooper());
                } finally {
                    event.set();
                }
                Looper.loop();
            }
        });
        thread.start();

        boolean interrupted = false;
        for (; ; ) {
            try {
                event.await();
                break;
            } catch (final InterruptedException e) {
                interrupted = true;
            }
        }

        final Looper looper = looperTransit.get();
        if (looper == null) {
            throw new RuntimeException("Unexpected error occurred.");
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }

        return new MessageLoop(thread, looper);
    }
}
