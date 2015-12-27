package tv.loilo.promise;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Junpei on 2015/10/07.
 */
public final class Dispatcher {

    private static Dispatcher mMainDispatcher = null;
    private final Handler mHandler;

    public Dispatcher(Looper looper) {
        mHandler = new Handler(looper);
    }

    public static Dispatcher getMainDispatcher() {
        Dispatcher dispatcher = mMainDispatcher;
        if (dispatcher == null) {
            //ダブルチェックロッキング
            synchronized (Dispatcher.class) {
                if (mMainDispatcher == null) {
                    mMainDispatcher = new Dispatcher(Looper.getMainLooper());
                }
                dispatcher = mMainDispatcher;
            }
        }
        return dispatcher;
    }

    public boolean isCurrentThread() {
        return mHandler.getLooper().getThread() == Thread.currentThread();
    }

    public void run(final Runnable runnable) {
        if (isCurrentThread()) {
            runnable.run();
        } else {
            mHandler.post(runnable);
        }
    }

    public void post(final Runnable runnable) {
        mHandler.post(runnable);
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
                    //この中に入るとcanceledフラグではもうキャンセルできない
                    final T result = callable.call();
                    deferrable.setSucceeded(result);
                } catch (final CancellationException e) {
                    deferrable.setCanceled();
                } catch (final Exception e) {
                    deferrable.setFailed(e);
                }
            }
        });

        return deferrable;
    }
}
