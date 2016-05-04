package tv.loilo.promise.http;

public class HttpResponseException extends RuntimeException {

    private final int mCode;

    public HttpResponseException(final int code) {
        mCode = code;
    }

    public HttpResponseException(final int code, final String detailMessage) {
        super(detailMessage);
        mCode = code;
    }

    public HttpResponseException(final int code, final String detailMessage, final Throwable throwable) {
        super(detailMessage, throwable);
        mCode = code;
    }

    public HttpResponseException(final int code, final Throwable throwable) {
        super(throwable);
        mCode = code;
    }

    public int getCode() {
        return mCode;
    }
}
