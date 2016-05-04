package tv.loilo.promise.http;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

import okhttp3.Headers;

public interface ResponseUnit {
    int getCode();

    Headers getHeaders();

    @NonNull
    Date getLocalDate();

    @Nullable
    Date getServerDate();
}
