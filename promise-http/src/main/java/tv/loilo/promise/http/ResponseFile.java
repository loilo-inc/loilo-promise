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

import java.io.File;
import java.util.Date;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;

public class ResponseFile extends ResponseAs<File> {

    private final MediaType mContentType;
    private final long mContentLength;

    public ResponseFile(String requestMethod, HttpUrl requestUrl, long sentRequestAtMillis, long receivedResponseAtMillis, int code, Headers headers, Date localDate, File file, MediaType contentType, long contentLength) {
        super(requestMethod, requestUrl, sentRequestAtMillis, receivedResponseAtMillis, code, headers, localDate, file);
        mContentType = contentType;
        mContentLength = contentLength;
    }

    public MediaType getContentType() {
        return mContentType;
    }

    public long getContentLength() {
        return mContentLength;
    }
}