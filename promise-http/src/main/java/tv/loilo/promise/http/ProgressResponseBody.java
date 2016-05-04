package tv.loilo.promise.http;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.CancellationException;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;
import tv.loilo.promise.ProgressReporter;

public final class ProgressResponseBody extends ResponseBody {

    @NonNull
    private final Call mCall;
    private final int mCode;
    @NonNull
    private final ResponseBody mBody;
    @Nullable
    private final ProgressReporter<HttpProgress> mReporter;
    @Nullable
    private BufferedSource mBufferedSource;

    public ProgressResponseBody(@NonNull final Call call, final int code, @NonNull final ResponseBody body, @Nullable final ProgressReporter<HttpProgress> reporter) {
        mCall = call;
        mCode = code;
        mBody = body;
        mReporter = reporter;
    }

    public ProgressResponseBody(@NonNull final Call call, final int code, @NonNull final ResponseBody body) {
        this(call, code, body, null);
    }

    @Override
    public MediaType contentType() {
        return mBody.contentType();
    }

    @Override
    public long contentLength() {
        return mBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (mBufferedSource == null) {
            mBufferedSource = Okio.buffer(wrap(mBody.source()));
        }
        return mBufferedSource;
    }

    @NonNull
    private Source wrap(@NonNull final Source source) {
        return new ForwardingSource(source) {
            private long mTotalBytes = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                if (mCall.isCanceled()) {
                    throw new CancellationException();
                }
                final long bytes = super.read(sink, byteCount);
                if (bytes > 0) {
                    mTotalBytes += bytes;
                }
                if (mReporter != null) {
                    mReporter.report(new HttpProgress(HttpProgress.Phase.RESPONSE, mCode, mTotalBytes, mBody.contentLength()));
                }
                return bytes;
            }
        };
    }
}
