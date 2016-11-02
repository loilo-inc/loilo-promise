/*
 * Copyright (c) 2015-2016 LoiLo inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.loilo.promise.http;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.MimeTypeMap;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Response;

public final class HttpUtils {
    private HttpUtils() {
    }

    public static boolean isSuccessful(final int code) {
        return code >= 200 && code < 300;
    }

    public static boolean canRetry(final int code) {
        return code == 500 || //Internal Server Error
                code == 502 || //Bad Gateway
                code == 503 || //Service Unavailable
                code == 504 || //Gateway Timeout
                code == 509; //Bandwidth Limit Exceeded
    }

    public static void ensureSuccessStatusCode(final int code) throws HttpResponseException {
        if (isSuccessful(code)) {
            return;
        }

        throw new HttpResponseException(code, Integer.toString(code));
    }

    public static void ensureSuccessStatusCode(@NonNull final Response response) throws HttpResponseException {
        final int code = response.code();
        if (isSuccessful(code)) {
            return;
        }

        throw new HttpResponseException(
                response.request().method(),
                response.request().url(),
                response.sentRequestAtMillis(),
                response.receivedResponseAtMillis(),
                response.protocol(),
                code,
                response.message(),
                response.headers(),
                String.valueOf(response.protocol()).toUpperCase() + " " + code + " " + response.message());
    }

    public static void ensureSuccessStatusCode(@NonNull final ResponseUnit response) throws HttpResponseException {
        final int code = response.getCode();
        if (isSuccessful(code)) {
            return;
        }

        throw new HttpResponseException(
                response.getRequestMethod(),
                response.getRequestUrl(),
                response.getSentRequestAtMillis(),
                response.getReceivedResponseAtMillis(),
                response.getProtocol(),
                code,
                response.getMessage(),
                response.getHeaders(),
                String.valueOf(response.getProtocol()).toUpperCase() + " " + code + " " + response.getMessage());
    }

    @NonNull
    public static MediaType getContentType(@Nullable final String extension) {
        if (extension != null && extension.length() > 1) {
            final String extensionWithoutPeriod = extension.substring(1);
            final String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extensionWithoutPeriod);
            if (mimeType != null) {
                return MediaType.parse(mimeType);
            }
        }
        return MediaType.parse("application/octet-stream");
    }

    @Nullable
    private static String getContentFileExtensionFromContentType(@Nullable final MediaType mediaType) {
        if (mediaType != null) {
            final String fromMimeType = MimeTypeMap.getSingleton().getExtensionFromMimeType(mediaType.toString());
            if (fromMimeType != null) {
                return "." + fromMimeType;
            }
        }

        return null;
    }

    @Nullable
    private static String getLastSegment(@Nullable final String path, final char separator) {
        if (path == null) {
            return null;
        }

        final int index = path.lastIndexOf(separator);
        if (index < 0) {
            return null;
        }
        return path.substring(index);
    }

    @NonNull
    private static String getExtension(@Nullable final String fileName) {
        final String lastSegment = getLastSegment(fileName, '.');
        if (lastSegment == null) {
            return "";
        }
        return lastSegment;
    }

    @Nullable
    private static String getContentFileExtensionFromContentDisposition(@Nullable final String contentDisposition) {
        final HttpContentDisposition httpContentDisposition = HttpContentDisposition.tryParse(contentDisposition);
        if (httpContentDisposition == null) {
            return null;
        }

        final String fileNameStar = httpContentDisposition.getFileNameStar();
        if (fileNameStar != null) {
            return getExtension(fileNameStar);
        }

        final String fileName = httpContentDisposition.getFileName();
        if (fileName != null) {
            return getExtension(fileName);
        }

        return null;
    }

    @Nullable
    private static String getContentFileExtensionFromContentDisposition(@NonNull final Headers headers) {
        final String contentDisposition = headers.get("Content-Disposition");
        return getContentFileExtensionFromContentDisposition(contentDisposition);
    }

    @NonNull
    public static String getContentFileExtension(@NonNull final Headers headers, @Nullable final MediaType contentType) {

        final String fromContentDisposition = getContentFileExtensionFromContentDisposition(headers);
        if (fromContentDisposition != null) {
            return fromContentDisposition;
        }

        final String fromContentType = getContentFileExtensionFromContentType(contentType);
        if (fromContentType != null) {
            return fromContentType;
        }

        return "";
    }

    @NonNull
    public static String getContentFileExtension(@NonNull final Response response) {
        return getContentFileExtension(response.headers(), response.body().contentType());
    }

}
