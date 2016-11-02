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

import java.util.Date;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Protocol;

public class ResponseString extends ResponseAs<String> {
    public ResponseString(
            String requestMethod,
            HttpUrl requestUrl,
            long sentRequestAtMillis,
            long receivedResponseAtMillis,
            final Protocol protocol,
            final int code,
            final String message,
            Headers headers,
            Date localDate,
            String s) {
        super(requestMethod, requestUrl, sentRequestAtMillis, receivedResponseAtMillis, protocol, code, message, headers, localDate, s);
    }
}
