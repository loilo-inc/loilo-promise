package tv.loilo.promise.http;

import android.support.annotation.NonNull;

import java.util.Date;

import okhttp3.Response;

public final class ResponseNoBody implements ResponseFilter<ResponseUnit> {
    private final boolean mAllowErrorCode;

    public ResponseNoBody(final boolean allowErrorCode) {
        mAllowErrorCode = allowErrorCode;
    }

    public ResponseNoBody() {
        this(false);
    }

    @Override
    public ResponseUnit pass(@NonNull Response response) throws Exception {
        final Date localDate = new Date();
        if (!mAllowErrorCode) {
            HttpUtils.ensureSuccessStatusCode(response);
        }
        response.body().close();
        return new ResponseAs<>(response.code(), response.headers(), localDate, null);
    }
}
