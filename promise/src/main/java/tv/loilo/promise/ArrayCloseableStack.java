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
