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

import java.util.Date;

import okhttp3.Response;

public final class ResponseStringer implements ResponseFilter<ResponseAs<String>> {
    private final boolean mAllowErrorCode;

    public ResponseStringer(final boolean allowErrorCode) {
        mAllowErrorCode = allowErrorCode;
    }

    public ResponseStringer() {
        this(false);
    }

    @Override
    public ResponseAs<String> pass(@NonNull Response response) throws Exception {
        final Date localDate = new Date();
        if (!mAllowErrorCode) {
            HttpUtils.ensureSuccessStatusCode(response);
        }

        final String body = response.body().string();
        return new ResponseAs<>(response.code(), response.headers(), localDate, body);
    }
}
