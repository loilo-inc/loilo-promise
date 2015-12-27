package tv.loilo.promise;

import android.util.Log;

import java.io.Closeable;
import java.util.ArrayDeque;
import java.util.Iterator;

/**
 * Created by Junpei on 2015/06/16.
 */
public final class ArrayCloseableStack implements CloseableStack, Closeable {
    private final ArrayDeque<Closeable> mDeque;

    public ArrayCloseableStack() {
        mDeque = new ArrayDeque<>(0);
    }

    @Override
    public <T extends Closeable> T push(T closeable) {
        mDeque.add(closeable);
        return closeable;
    }

    @Override
    public void close() {
        for (Iterator<Closeable> ite = mDeque.descendingIterator(); ite.hasNext(); ) {
            final Closeable closeable = ite.next();
            try {
                closeable.close();
            } catch (final Exception e) {
                Log.w("loilo-promise", "ArrayCloseableStack: Close error occurred.", e);
            }
        }
    }
}
