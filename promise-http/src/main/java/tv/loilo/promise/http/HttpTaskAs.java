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

import java.io.IOException;
import java.util.concurrent.CancellationException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import tv.loilo.promise.Cancellable;
import tv.loilo.promise.Deferrable;
import tv.loilo.promise.Deferred;
import tv.loilo.promise.ProgressReporter;
import tv.loilo.promise.Promise;
import tv.loilo.promise.Promises;
import tv.loilo.promise.WhenCallback;
import tv.loilo.promise.WhenParams;

public final class HttpTaskAs<TResponse> {

    @NonNull
    private final Call mCall;
    @NonNull
    private final ResponseFilter<TResponse> mFilter;
    @Nullable
    private ProgressReporter<HttpProgress> mReporter;

    public HttpTaskAs(@NonNull final Call call, @NonNull final ResponseFilter<TResponse> filter) {
        mCall = call;
        mFilter = filter;
    }

    public HttpTaskAs<TResponse> progress(@Nullable final ProgressReporter<HttpProgress> reporter) {
        mReporter = reporter;
        return this;
    }

    public Promise<TResponse> promise() {
        return Promises.when(new WhenCallback<TResponse>() {
            @Override
            public Deferred<TResponse> run(WhenParams params) throws Exception {
                final Deferrable<TResponse> deferrable = new Deferrable<>();
                deferrable.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() {
                        mCall.cancel();
                    }
                });
                mCall.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        if (call.isCanceled()) {
                            deferrable.setCanceled();
                        } else {
                            deferrable.setFailed(e);
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        TResponse result;
                        try {
                            Response progressResponse = null;
                            try {
                                progressResponse = response
                                        .newBuilder()
                                        .body(new ProgressResponseBody(call, response.code(), response.body(), mReporter))
                                        .build();
                                result = mFilter.pass(progressResponse);
                            } finally {
                                if (progressResponse != null) {
                                    progressResponse.body().close();
                                } else {
                                    response.body().close();
                                }
                            }
                        } catch (final CancellationException e) {
                            deferrable.setCanceled();
                            return;
                        } catch (final Exception e) {
                            deferrable.setFailed(e);
                            return;
                        }
                        deferrable.setSucceeded(result);
                    }
                });

                return deferrable;
            }
        });
    }

}
