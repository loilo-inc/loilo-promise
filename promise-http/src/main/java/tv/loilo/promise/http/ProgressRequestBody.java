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

package tv.loilo.promise.http;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;
import tv.loilo.promise.ProgressReporter;

public final class ProgressRequestBody extends RequestBody {

    @NonNull
    private final RequestBody mBody;
    @Nullable
    private final ProgressReporter<HttpProgress> mReporter;

    public ProgressRequestBody(@NonNull final RequestBody body, @Nullable final ProgressReporter<HttpProgress> reporter) {
        mBody = body;
        mReporter = reporter;
    }

    public ProgressRequestBody(@NonNull final RequestBody body) {
        this(body, null);
    }

    public RequestBody getOriginalBody() {
        return mBody;
    }

    @Override
    public MediaType contentType() {
        return mBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return mBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        //TODO もしかするとここでキャンセル処理をしないといけない
        final BufferedSink bufferedSink = Okio.buffer(wrap(sink));
        mBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    @NonNull
    private Sink wrap(@NonNull final Sink sink) {
        return new ForwardingSink(sink) {
            private long mTotalBytes = 0L;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                mTotalBytes += byteCount;
                if (mReporter != null) {
                    mReporter.report(new HttpProgress(HttpProgress.Phase.REQUEST, 0, mTotalBytes, mBody.contentLength()));
                }
            }
        };
    }
}
