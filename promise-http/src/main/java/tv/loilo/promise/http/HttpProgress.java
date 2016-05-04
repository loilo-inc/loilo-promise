package tv.loilo.promise.http;

import android.support.annotation.Nullable;

import tv.loilo.promise.Progress;

public final class HttpProgress implements Progress {

    private final Phase mPhase;
    private final int mCode;
    private final long mBytesProceeded;
    private final long mContentLength;
    private String mMessage;

    public HttpProgress(final Phase phase, final int code, final long bytesProceeded, final long contentLength) {
        mPhase = phase;
        mCode = code;
        mBytesProceeded = bytesProceeded;
        mContentLength = contentLength;
    }

    public Phase getPhase() {
        return mPhase;
    }

    public int getCode() {
        return mCode;
    }

    public long getBytesProceeded() {
        return mBytesProceeded;
    }

    public long getContentLength() {
        return mContentLength;
    }

    @Override
    public int getCurrent() {
        return getCurrent(mBytesProceeded, mContentLength);
    }

    @Override
    public double getPercentage() {
        return getPercentage(mBytesProceeded, mContentLength);
    }

    @Override
    public long getRawCurrent() {
        return getBytesProceeded();
    }

    @Override
    public long getRawMax() {
        return getContentLength();
    }

    @Nullable
    @Override
    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public enum Phase {
        REQUEST,
        RESPONSE
    }

    public static double getPercentage(long rawCurrent, long rawMax) {
        if (rawMax <= 0) {
            return 0;
        }
        return (double) rawCurrent / (double) rawMax;
    }

    public static int getCurrent(long rawCurrent, long rawMax) {
        final double percentage = getPercentage(rawCurrent, rawMax);
        return Math.max(0, Math.min(MAX, (int) Math.round(MAX * percentage)));
    }

    @Override
    public String toString() {
        return "HttpProgress{" +
                "mPhase=" + mPhase +
                ", mCode=" + mCode +
                ", mBytesProceeded=" + mBytesProceeded +
                ", mContentLength=" + mContentLength +
                ", mMessage='" + mMessage + '\'' +
                '}';
    }
}
