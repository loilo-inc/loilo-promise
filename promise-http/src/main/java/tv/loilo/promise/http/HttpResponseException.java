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

import android.support.annotation.Nullable;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Protocol;

public class HttpResponseException extends RuntimeException {

    @Nullable
    private final String mRequestMethod;
    @Nullable
    private final HttpUrl mRequestUrl;
    private final long mSentRequestAtMillis;
    private final long mReceivedResponseAtMillis;
    @Nullable
    private final Protocol mProtocol;
    private final int mCode;
    @Nullable
    private final String mResponseMessage;
    @Nullable
    private final Headers mHeaders;

    public HttpResponseException(@Nullable final String requestMethod,
                                 @Nullable final HttpUrl requestUrl,
                                 final long sentRequestAtMillis,
                                 final long receivedResponseAtMillis,
                                 @Nullable final Protocol protocol,
                                 final int code,
                                 @Nullable final String responseMessage,
                                 @Nullable final Headers headers) {
        mRequestMethod = requestMethod;
        mRequestUrl = requestUrl;
        mSentRequestAtMillis = sentRequestAtMillis;
        mReceivedResponseAtMillis = receivedResponseAtMillis;
        mProtocol = protocol;
        mCode = code;
        mResponseMessage = responseMessage;
        mHeaders = headers;
    }

    public HttpResponseException(@Nullable final String requestMethod,
                                 @Nullable final HttpUrl requestUrl,
                                 final long sentRequestAtMillis,
                                 final long receivedResponseAtMillis,
                                 @Nullable final Protocol protocol,
                                 final int code,
                                 @Nullable final String responseMessage,
                                 @Nullable final Headers headers,
                                 final String detailMessage) {
        super(detailMessage);
        mRequestMethod = requestMethod;
        mRequestUrl = requestUrl;
        mSentRequestAtMillis = sentRequestAtMillis;
        mReceivedResponseAtMillis = receivedResponseAtMillis;
        mProtocol = protocol;
        mCode = code;
        mResponseMessage = responseMessage;
        mHeaders = headers;
    }

    public HttpResponseException(@Nullable final String requestMethod,
                                 @Nullable final HttpUrl requestUrl,
                                 final long sentRequestAtMillis,
                                 final long receivedResponseAtMillis,
                                 @Nullable final Protocol protocol,
                                 final int code,
                                 @Nullable final String responseMessage,
                                 @Nullable final Headers headers,
                                 final String detailMessage,
                                 final Throwable throwable) {
        super(detailMessage, throwable);
        mRequestMethod = requestMethod;
        mRequestUrl = requestUrl;
        mSentRequestAtMillis = sentRequestAtMillis;
        mReceivedResponseAtMillis = receivedResponseAtMillis;
        mProtocol = protocol;
        mCode = code;
        mResponseMessage = responseMessage;
        mHeaders = headers;
    }

    public HttpResponseException(@Nullable final String requestMethod,
                                 @Nullable final HttpUrl requestUrl,
                                 final long sentRequestAtMillis,
                                 final long receivedResponseAtMillis,
                                 @Nullable final Protocol protocol,
                                 final int code,
                                 @Nullable final String responseMessage,
                                 @Nullable final Headers headers,
                                 final Throwable throwable) {
        super(throwable);
        mRequestMethod = requestMethod;
        mRequestUrl = requestUrl;
        mSentRequestAtMillis = sentRequestAtMillis;
        mReceivedResponseAtMillis = receivedResponseAtMillis;
        mProtocol = protocol;
        mCode = code;
        mResponseMessage = responseMessage;
        mHeaders = headers;
    }

    public HttpResponseException(final int code) {
        this(null, null, -1L, -1L, null, code, null, null);
    }

    public HttpResponseException(final int code, final String detailMessage) {
        this(null, null, -1L, -1L, null, code, null, null, detailMessage);
    }

    public HttpResponseException(final int code, final String detailMessage, final Throwable throwable) {
        this(null, null, -1L, -1L, null, code, null, null, detailMessage, throwable);
    }

    public HttpResponseException(final int code, final Throwable throwable) {
        this(null, null, -1L, -1L, null, code, null, null, throwable);
    }

    @Nullable
    public String getRequestMethod() {
        return mRequestMethod;
    }

    @Nullable
    public HttpUrl getRequestUrl() {
        return mRequestUrl;
    }

    public long getSentRequestAtMillis() {
        return mSentRequestAtMillis;
    }

    public long getReceivedResponseAtMillis() {
        return mReceivedResponseAtMillis;
    }

    @Nullable
    public Protocol getProtocol() {
        return mProtocol;
    }

    public int getCode() {
        return mCode;
    }

    @Nullable
    public String getResponseMessage() {
        return mResponseMessage;
    }

    @Nullable
    public Headers getHeaders() {
        return mHeaders;
    }
}
