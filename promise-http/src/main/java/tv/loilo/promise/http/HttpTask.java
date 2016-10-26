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

import okhttp3.Call;
import okhttp3.MediaType;

public final class HttpTask {
    private Call mCall;
    private ResponseUnitMonitor.OnResponseListener mOnResponseListener;

    public HttpTask(Call call) {
        mCall = call;
    }

    public HttpTask setOnResponseListener(ResponseUnitMonitor.OnResponseListener listener) {
        mOnResponseListener = listener;
        return this;
    }

    public <TValue> HttpTaskAs<TValue> filterBy(@NonNull final ResponseFilter<TValue> filter) {
        return new HttpTaskAs<>(mCall, filter);
    }

    public <TValue extends ResponseUnit> HttpTaskAs<TValue> asResponseUnitBy(@NonNull final ResponseFilter<TValue> filter) {
        final ResponseUnitMonitor.OnResponseListener listener = mOnResponseListener;
        if (listener != null) {
            return filterBy(new ResponseUnitMonitor<>(filter, listener));
        }
        return filterBy(filter);
    }

    public HttpTaskAs<ResponseNoBody> noBody(final boolean allowErrorCode) {
        return asResponseUnitBy(new ResponseNoBodyFilter(allowErrorCode));
    }

    public HttpTaskAs<ResponseNoBody> noBody() {
        return noBody(false);
    }

    public HttpTaskAs<ResponseString> asString(final boolean allowErrorCode) {
        return asResponseUnitBy(new ResponseStringer(allowErrorCode));
    }

    public HttpTaskAs<ResponseString> asString() {
        return asString(false);
    }

    public HttpTaskAs<ResponseJsonObject> asJsonObject(final boolean allowErrorCodeIfPossible) {
        return asResponseUnitBy(new ResponseJsonObjectConverter(allowErrorCodeIfPossible));
    }

    public HttpTaskAs<ResponseJsonObject> asJsonObject() {
        return asJsonObject(false);
    }

    public HttpTaskAs<ResponseJsonArray> asJsonArray(final boolean allowErrorCodeIfPossible) {
        return asResponseUnitBy(new ResponseJsonArrayConverter(allowErrorCodeIfPossible));
    }

    public HttpTaskAs<ResponseJsonArray> asJsonArray() {
        return asJsonArray(false);
    }

    public HttpTaskAs<ResponseFile> writeTo(final File output) {
        return asResponseUnitBy(new ResponseFileWriter(output));
    }

    @Deprecated
    public HttpTaskAs<ResponseAs<MediaType>> writeToFile(final File output) {
        return asResponseUnitBy(new ResponseFileExporter(output));
    }
}
