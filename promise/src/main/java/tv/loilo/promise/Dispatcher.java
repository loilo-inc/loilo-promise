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

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The android.os.Handler wrapper class.
 */
public final class Dispatcher {

    private static Dispatcher mMainDispatcher = null;
    private static MessageLoop mSubMessageLoop = null;
    private static Dispatcher mSubDispatcher = null;

    private final Handler mHandler;

    public Dispatcher(Looper looper) {
        mHandler = new Handler(looper);
    }

    public static Dispatcher getMainDispatcher() {
        Dispatcher dispatcher = mMainDispatcher;
        if (dispatcher == null) {
            //Double check locking.
            synchronized (Dispatcher.class) {
                if (mMainDispatcher == null) {
                    mMainDispatcher = new Dispatcher(Looper.getMainLooper());
                }
                dispatcher = mMainDispatcher;
            }
        }
        return dispatcher;
    }

    public static Dispatcher getSubDispatcher() {
        Dispatcher dispatcher = mSubDispatcher;
        if (dispatcher == null) {
            //Double check locking.
            synchronized (Dispatcher.class) {
                if (mSubDispatcher == null) {
                    if (mSubMessageLoop == null) {
                        mSubMessageLoop = MessageLoop.run();
                    }
                    mSubDispatcher = new Dispatcher(mSubMessageLoop.getLooper());
                }
                dispatcher = mSubDispatcher;
            }
        }
        return dispatcher;
    }

    public boolean isCurrentThread() {
        return mHandler.getLooper().getThread() == Thread.currentThread();
    }

    public void run(Runnable runnable) {
        if (isCurrentThread()) {
            runnable.run();
        } else {
            mHandler.post(runnable);
        }
    }

    public void post(Runnable runnable) {
        mHandler.post(runnable);
    }

    public void post(Runnable runnable, long delayMills){
        mHandler.postDelayed(runnable, delayMills);
    }

    public void remove(Runnable runnable){
        mHandler.removeCallbacks(runnable);
    }

    public <T> Deferred<T> call(final Callable<T> callable) {
        final Deferrable<T> deferrable = new Deferrable<>();
        final AtomicBoolean canceled = new AtomicBoolean();
        deferrable.setCancellable(new Cancellable() {
            @Override
            public void cancel() {
                canceled.set(true);
            }
        });

        run(new Runnable() {
            @Override
            public void run() {
                if (canceled.get()) {
                    deferrable.setCanceled();
                    return;
                }
                try {
                    //The callable can not cancel by the canceled flag.
                    final T result = callable.call();
                    deferrable.setSucceeded(result);
                } catch (final CancellationException e) {
                    deferrable.setCanceled();
                } catch (final Throwable e) {
                    deferrable.setFailed(e);
                }
            }
        });

        return deferrable;
    }
}
