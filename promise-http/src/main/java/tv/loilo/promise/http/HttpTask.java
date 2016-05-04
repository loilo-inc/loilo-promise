package tv.loilo.promise.http;

import android.support.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;

import okhttp3.Call;
import okhttp3.MediaType;

public final class HttpTask {
    private Call mCall;

    public HttpTask(Call call) {
        mCall = call;
    }

    public <TValue> HttpTaskAs<TValue> filterBy(@NonNull final ResponseFilter<TValue> filter) {
        return new HttpTaskAs<>(mCall, filter);
    }

    public HttpTaskAs<ResponseUnit> noBody(final boolean allowErrorCode) {
        return filterBy(new ResponseNoBody(allowErrorCode));
    }

    public HttpTaskAs<ResponseUnit> noBody() {
        return noBody(false);
    }

    public HttpTaskAs<ResponseAs<String>> asString(final boolean allowErrorCode) {
        return filterBy(new ResponseStringer(allowErrorCode));
    }

    public HttpTaskAs<ResponseAs<String>> asString() {
        return asString(false);
    }

    public HttpTaskAs<ResponseAs<JsonObject>> asJsonObject(final boolean allowErrorCodeIfPossible) {
        return filterBy(new ResponseJsonObjectConverter(allowErrorCodeIfPossible));
    }

    public HttpTaskAs<ResponseAs<JsonObject>> asJsonObject() {
        return asJsonObject(false);
    }

    public HttpTaskAs<ResponseAs<JsonArray>> asJsonArray(final boolean allowErrorCodeIfPossible) {
        return filterBy(new ResponseJsonArrayConverter(allowErrorCodeIfPossible));
    }

    public HttpTaskAs<ResponseAs<JsonArray>> asJsonArray() {
        return asJsonArray(false);
    }

    public HttpTaskAs<ResponseAs<MediaType>> writeToFile(final File output) {
        return filterBy(new ResponseFileExporter(output));
    }
}
