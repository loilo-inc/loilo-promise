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
        if (mPhase == Phase.RESPONSE) {
            return String.valueOf(mPhase) + "(" + mCode + ") " + mBytesProceeded + "/" + mContentLength + " bytes";
        }

        return String.valueOf(mPhase) + " " + mBytesProceeded + "/" + mContentLength + " bytes";
    }
}
