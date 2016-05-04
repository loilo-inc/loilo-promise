package tv.loilo.promise.http;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

@SuppressWarnings("TryFinallyCanBeTryWithResources")
public final class ResponseFileExporter implements ResponseFilter<ResponseAs<MediaType>> {

    private final File mOutput;

    public ResponseFileExporter(@NonNull final File output) {
        mOutput = output;
    }

    @Override
    public ResponseAs<MediaType> pass(@NonNull Response response) throws Exception {
        final Date localDate = new Date();

        HttpUtils.ensureSuccessStatusCode(response);

        if (response.code() != 200 && response.code() != 206) {
            throw new HttpResponseException(response.code());
        }

        final boolean isAppending = response.code() == 206;

        final MediaType contentType = response.body().contentType();
        final BufferedSource source = response.body().source();
        try {
            final BufferedSink sink = Okio.buffer(isAppending ? Okio.appendingSink(mOutput) : Okio.sink(mOutput));
            try {
                sink.writeAll(source);
                sink.flush();
            } finally {
                sink.close();
            }
        } finally {
            source.close();
        }

        return new ResponseAs<>(response.code(), response.headers(), localDate, contentType);
    }
}
