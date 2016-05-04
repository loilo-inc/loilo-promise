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
