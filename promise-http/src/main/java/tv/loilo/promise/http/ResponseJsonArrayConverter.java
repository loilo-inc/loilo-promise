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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.Date;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Protocol;

public final class ResponseJsonArrayConverter extends ResponseJsonConverter<JsonArray, ResponseJsonArray> {
    public ResponseJsonArrayConverter(boolean allowErrorCodeIfPossible) {
        super(allowErrorCodeIfPossible);
    }

    @NonNull
    @Override
    protected ResponseJsonArray createResponse(String requestMethod, HttpUrl requestUrl, long sentRequestAtMillis, long receivedResponseAtMillis, Protocol protocol, int code, String message, Headers headers, @NonNull Date localDate, @NonNull JsonElement element) {
        final JsonArray jsonArray = element.getAsJsonArray();
        return new ResponseJsonArray(requestMethod, requestUrl, sentRequestAtMillis, receivedResponseAtMillis, protocol, code, message, headers, localDate, jsonArray);
    }
}
