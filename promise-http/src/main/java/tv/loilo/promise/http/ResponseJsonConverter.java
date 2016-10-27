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

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.Reader;
import java.util.Date;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Response;

@SuppressWarnings("TryFinallyCanBeTryWithResources")
public abstract class ResponseJsonConverter<TJson extends JsonElement, TResponse extends ResponseAs<TJson>> implements ResponseFilter<TResponse> {

    private final boolean mAllowErrorCode;

    public ResponseJsonConverter(final boolean allowErrorCodeIfPossible) {
        mAllowErrorCode = allowErrorCodeIfPossible;
    }

    @NonNull
    protected abstract TResponse createResponse(final String requestMethod,
                                         final HttpUrl requestUrl,
                                         final long sentRequestAtMillis,
                                         final long receivedResponseAtMillis,
                                         final int code,
                                         final Headers headers,
                                         @NonNull final Date localDate,
                                         @NonNull final JsonElement element);


    @NonNull
    public static JsonElement parse(@NonNull final JsonReader json) throws JsonIOException, JsonSyntaxException {
        boolean lenient = json.isLenient();
        json.setLenient(true);
        try {
            return Streams.parse(json);
        } catch (final StackOverflowError | OutOfMemoryError e) {
            throw new JsonIOException("Failed parsing JSON source: " + json + " to Json", e);
        } finally {
            json.setLenient(lenient);
        }
    }

    @NonNull
    public static JsonElement parse(@NonNull final Reader json) throws JsonIOException, JsonSyntaxException, IOException {
        final JsonReader jsonReader = new JsonReader(json);
        try {
            return parse(jsonReader);
        } finally {
            jsonReader.close();
        }
    }

    @Nullable
    @Override
    public TResponse pass(@NonNull Response response) throws Exception {
        final Date localDate = new Date();
        boolean statusCodeChecked = false;
        if (!mAllowErrorCode) {
            HttpUtils.ensureSuccessStatusCode(response);
            statusCodeChecked = true;
        }

        final MediaType mediaType = response.body().contentType();
        if (mediaType == null) {
            if (!statusCodeChecked) {
                HttpUtils.ensureSuccessStatusCode(response);
            }
        } else if (!"application".equals(mediaType.type()) || !"json".equals(mediaType.subtype())) {
            if (!statusCodeChecked) {
                HttpUtils.ensureSuccessStatusCode(response);
            }
        }

        final Reader responseReader = response.body().charStream();
        try {
            final JsonElement element = parse(responseReader);
            return createResponse(
                    response.request().method(),
                    response.request().url(),
                    response.sentRequestAtMillis(),
                    response.receivedResponseAtMillis(),
                    response.code(),
                    response.headers(),
                    localDate,
                    element);
        } finally {
            responseReader.close();
        }
    }
}
