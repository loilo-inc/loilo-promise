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

import java.io.File;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

@SuppressWarnings("TryFinallyCanBeTryWithResources")
public class ResponseFileWriter implements ResponseFilter<ResponseFile> {

    private final File mOutput;

    public ResponseFileWriter(@NonNull final File output) {
        mOutput = output;
    }

    @Override
    public ResponseFile pass(@NonNull Response response) throws Exception {
        final Date localDate = new Date();

        HttpUtils.ensureSuccessStatusCode(response);

        if (response.code() != 200 && response.code() != 206) {
            throw new HttpResponseException(response.code());
        }

        final boolean isAppending = response.code() == 206;

        final MediaType contentType = response.body().contentType();
        final long contentLength = response.body().contentLength();
        final BufferedSource source = response.body().source();
        try {
            final BufferedSink sink = Okio.buffer(isAppending ? Okio.appendingSink(mOutput) : Okio.sink(mOutput));
            try {
                sink.writeAll(source);
                sink.flush();
            } finally {
                sink.close();
            }
        } finally {
            source.close();
        }

        return new ResponseFile(
                response.request().method(),
                response.request().url(),
                response.sentRequestAtMillis(),
                response.receivedResponseAtMillis(),
                response.protocol(),
                response.code(),
                response.message(),
                response.headers(),
                localDate,
                mOutput,
                contentType,
                contentLength);
    }
}
