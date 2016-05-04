package tv.loilo.promise.http;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

import okhttp3.Headers;

public final class ResponseAs<TBody> implements ResponseUnit {

    private final int mCode;
    private final Headers mHeaders;
    private final Date mLocalDate;
    private final TBody mBody;

    public ResponseAs(final int code, final Headers headers, final Date localDate, final TBody body) {
        mCode = code;
        mHeaders = headers;
        mLocalDate = localDate;
        mBody = body;
    }

    @Override
    public int getCode() {
        return mCode;
    }

    @Override
    public Headers getHeaders() {
        return mHeaders;
    }

    @Override
    @NonNull
    public Date getLocalDate() {
        return mLocalDate;
    }

    @Override
    @Nullable
    public Date getServerDate() {
        if (mHeaders == null) {
            return null;
        }
        return mHeaders.getDate("Date");
    }

    public TBody getBody() {
        return mBody;
    }

    @Override
    public String toString() {
        return "ResponseAs{" +
                "mCode=" + mCode +
                ", mHeaders=" + mHeaders +
                ", mLocalDate=" + mLocalDate +
                ", mBody=" + mBody +
                '}';
    }
}
