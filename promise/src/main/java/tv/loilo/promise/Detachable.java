package tv.loilo.promise;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Junpei on 2015/09/17.
 */
public final class Detachable<TRef extends Closeable> implements Closeable {

    private TRef mRef;

    public Detachable(TRef ref) {
        mRef = ref;
    }

    public Detachable() {
    }

    public TRef ref() {
        return mRef;
    }

    public boolean isAttached() {
        return mRef != null;
    }

    public void attach(TRef ref) {
        if (mRef != null) {
            throw new UnsupportedOperationException();
        }
        mRef = ref;
    }

    public TRef detach() {
        final TRef ref = mRef;
        mRef = null;
        return ref;
    }

    @Override
    public void close() throws IOException {
        final TRef ref = detach();
        if (ref != null) {
            ref.close();
        }
    }
}
