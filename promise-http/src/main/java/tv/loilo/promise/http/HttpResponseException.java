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

public class HttpResponseException extends RuntimeException {

    private final int mCode;

    public HttpResponseException(final int code) {
        mCode = code;
    }

    public HttpResponseException(final int code, final String detailMessage) {
        super(detailMessage);
        mCode = code;
    }

    public HttpResponseException(final int code, final String detailMessage, final Throwable throwable) {
        super(detailMessage, throwable);
        mCode = code;
    }

    public HttpResponseException(final int code, final Throwable throwable) {
        super(throwable);
        mCode = code;
    }

    public int getCode() {
        return mCode;
    }
}
