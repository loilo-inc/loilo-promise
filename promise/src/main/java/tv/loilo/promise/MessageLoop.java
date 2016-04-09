package tv.loilo.promise;

import android.os.Looper;

import java.util.concurrent.atomic.AtomicReference;

public class MessageLoop {

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
            } catch (InterruptedException e) {
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
