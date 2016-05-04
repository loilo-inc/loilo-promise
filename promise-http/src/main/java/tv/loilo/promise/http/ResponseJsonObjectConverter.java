package tv.loilo.promise.http;

import android.support.annotation.NonNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class ResponseJsonObjectConverter extends ResponseJsonConverter<JsonObject> {
    public ResponseJsonObjectConverter(boolean allowErrorCodeIfPossible) {
        super(allowErrorCodeIfPossible);
    }

    @NonNull
    @Override
    protected JsonObject convert(@NonNull final JsonElement element) {
        return element.getAsJsonObject();
    }
}
