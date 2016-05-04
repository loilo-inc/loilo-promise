package tv.loilo.promise.http;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.Reader;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.Response;

@SuppressWarnings("TryFinallyCanBeTryWithResources")
public abstract class ResponseJsonConverter<TJson extends JsonElement> implements ResponseFilter<ResponseAs<TJson>> {

    private final boolean mAllowErrorCode;

    public ResponseJsonConverter(final boolean allowErrorCodeIfPossible) {
        mAllowErrorCode = allowErrorCodeIfPossible;
    }

    @NonNull
    protected abstract TJson convert(@NonNull final JsonElement element);


    @NonNull
    public static JsonElement parse(@NonNull final JsonReader json) throws JsonIOException, JsonSyntaxException {
        boolean lenient = json.isLenient();
        json.setLenient(true);
        try {
            return Streams.parse(json);
        } catch (StackOverflowError | OutOfMemoryError e) {
            throw new JsonIOException("Failed parsing JSON source: " + json + " to Json", e);
        } finally {
            json.setLenient(lenient);
        }
    }

    @NonNull
    public static JsonElement parse(@NonNull final Reader json) throws JsonIOException, JsonSyntaxException, IOException {
        final JsonReader jsonReader = new JsonReader(json);
        try {
            return parse(jsonReader);
        } finally {
            jsonReader.close();
        }
    }

    @Nullable
    @Override
    public ResponseAs<TJson> pass(@NonNull Response response) throws Exception {
        final Date localDate = new Date();
        boolean statusCodeChecked = false;
        if (!mAllowErrorCode) {
            HttpUtils.ensureSuccessStatusCode(response);
            statusCodeChecked = true;
        }

        final MediaType mediaType = response.body().contentType();
        if (mediaType == null) {
            if (!statusCodeChecked) {
                HttpUtils.ensureSuccessStatusCode(response);
            }
        } else if (!"application".equals(mediaType.type()) || !"json".equals(mediaType.subtype())) {
            if (!statusCodeChecked) {
                HttpUtils.ensureSuccessStatusCode(response);
            }
        }

        final Reader responseReader = response.body().charStream();
        try {
            final JsonElement element = parse(responseReader);
            final TJson json = convert(element);
            return new ResponseAs<>(response.code(), response.headers(), localDate, json);
        } finally {
            responseReader.close();
        }
    }
}
