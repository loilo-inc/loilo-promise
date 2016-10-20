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

public class ResponseNoBody implements ResponseUnit {

    private final String mRequestMethod;
    private final HttpUrl mRequestUrl;
    private final long mSentRequestAtMillis;
    private final long mReceivedResponseAtMillis;
    private final int mCode;
    private final Headers mHeaders;
    private final Date mLocalDate;

    public ResponseNoBody(
            final String requestMethod,
            final HttpUrl requestUrl,
            final long sentRequestAtMillis,
            final long receivedResponseAtMillis,
            final int code,
            final Headers headers,
            final Date localDate) {
        mRequestMethod = requestMethod;
        mRequestUrl = requestUrl;
        mSentRequestAtMillis = sentRequestAtMillis;
        mReceivedResponseAtMillis = receivedResponseAtMillis;
        mCode = code;
        mHeaders = headers;
        mLocalDate = localDate;
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
    public int getCode() {
        return mCode;
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
        return "";
    }

    @Override
    public String toString() {
        return "ResponseNoBody{" +
                "mRequestMethod='" + mRequestMethod + '\'' +
                ", mRequestUrl=" + mRequestUrl +
                ", mSentRequestAtMillis=" + mSentRequestAtMillis +
                ", mReceivedResponseAtMillis=" + mReceivedResponseAtMillis +
                ", mCode=" + mCode +
                ", mHeaders=" + mHeaders +
                ", mLocalDate=" + mLocalDate +
                '}';
    }
}
