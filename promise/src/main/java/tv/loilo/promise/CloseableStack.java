package tv.loilo.promise;

import java.io.Closeable;

/**
 * Created by Junpei on 2015/06/16.
 */
public interface CloseableStack {
    <T extends Closeable> T push(T closeable);
}
