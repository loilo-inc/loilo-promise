package tv.loilo.promise.http;

import android.support.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public final class ResponseJsonArrayConverter extends ResponseJsonConverter<JsonArray> {
    public ResponseJsonArrayConverter(boolean allowErrorCodeIfPossible) {
        super(allowErrorCodeIfPossible);
    }

    @NonNull
    @Override
    protected JsonArray convert(@NonNull JsonElement element) {
        return element.getAsJsonArray();
    }
}
