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

import java.util.Date;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Protocol;

public class ResponseAs<TBody> implements ResponseUnit {

    private final String mRequestMethod;
    private final HttpUrl mRequestUrl;
    private final long mSentRequestAtMillis;
    private final long mReceivedResponseAtMillis;
    private final Protocol mProtocol;
    private final int mCode;
    private final String mMessage;
    private final Headers mHeaders;
    private final Date mLocalDate;
    private final TBody mBody;

    public ResponseAs(
            final String requestMethod,
            final HttpUrl requestUrl,
            final long sentRequestAtMillis,
            final long receivedResponseAtMillis,
            final Protocol protocol,
            final int code,
            final String message,
            final Headers headers,
            final Date localDate,
            final TBody body) {
        mRequestMethod = requestMethod;
        mRequestUrl = requestUrl;
        mSentRequestAtMillis = sentRequestAtMillis;
        mReceivedResponseAtMillis = receivedResponseAtMillis;
        mProtocol = protocol;
        mCode = code;
        mMessage = message;
        mHeaders = headers;
        mLocalDate = localDate;
        mBody = body;
    }

    @Override
    public String getRequestMethod() {
        return mRequestMethod;
    }

    @Override
    public HttpUrl getRequestUrl() {
        return mRequestUrl;
    }

    @Override
    public long getSentRequestAtMillis() {
        return mSentRequestAtMillis;
    }

    @Override
    public long getReceivedResponseAtMillis() {
        return mReceivedResponseAtMillis;
    }

    @Override
    public Protocol getProtocol() {
        return mProtocol;
    }

    @Override
    public int getCode() {
        return mCode;
    }

    @Override
    public String getMessage() {
        return mMessage;
    }

    @Override
    public Headers getHeaders() {
        return mHeaders;
    }

    @Override
    @NonNull
    public Date getLocalDate() {
        return mLocalDate;
    }

    @Override
    @Nullable
    public Date getServerDate() {
        if (mHeaders == null) {
            return null;
        }
        return mHeaders.getDate("Date");
    }

    @Override
    public String bodyToString() {
        return mBody == null ? "" : mBody.toString();
    }

    public TBody getBody() {
        return mBody;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(mProtocol).toUpperCase()).append(" ")
                .append(mCode).append(" ")
                .append(mMessage).append(" ")
                .append(mRequestMethod).append(" ")
                .append(mRequestUrl).append(" in ")
                .append(mReceivedResponseAtMillis - mSentRequestAtMillis).append(" ms\n")
                .append(mHeaders);
        final String bodyString = bodyToString();
        if (bodyString != null && bodyString.length() > 0) {
            sb.append("\n").append(bodyString);
        }
        return sb.toString();
    }
}
