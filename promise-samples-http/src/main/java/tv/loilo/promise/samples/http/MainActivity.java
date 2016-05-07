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

package tv.loilo.promise.samples.http;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import tv.loilo.promise.Defer;
import tv.loilo.promise.Deferred;
import tv.loilo.promise.Dispatcher;
import tv.loilo.promise.ProgressReporter;
import tv.loilo.promise.Promise;
import tv.loilo.promise.Promises;
import tv.loilo.promise.Result;
import tv.loilo.promise.SuccessCallback;
import tv.loilo.promise.SuccessParams;
import tv.loilo.promise.Transfer;
import tv.loilo.promise.WhenCallback;
import tv.loilo.promise.WhenParams;
import tv.loilo.promise.http.HttpProgress;
import tv.loilo.promise.http.HttpTask;
import tv.loilo.promise.http.ResponseAs;
import tv.loilo.promise.support.ProgressPromiseFactory;
import tv.loilo.promise.support.ProgressPromiseLoader;
import tv.loilo.promise.support.ProgressPromiseLoaderCallbacks;

public class MainActivity extends AppCompatActivity {

    private final ProgressPromiseLoaderCallbacks<List<String>, HttpProgress> mLoaderCallbacks = new ProgressPromiseLoaderCallbacks<List<String>, HttpProgress>() {
        @Override
        public void onLoaderProgress(int id, @NonNull HttpProgress httpProgress) {
            Log.d("loilo-promise", httpProgress.toString());
            if (mProgressBar != null) {
                mProgressBar.setProgress(httpProgress.getCurrent());
            }
        }

        @Override
        public Loader<Result<List<String>>> onCreateLoader(int id, Bundle args) {
            return ProgressPromiseLoader.createLoader(MainActivity.this, new ProgressPromiseFactory<List<String>, HttpProgress>() {
                @NonNull
                @Override
                public Promise<List<String>> createPromise(@NonNull final ProgressPromiseLoader<List<String>, HttpProgress> loader) {
                    return Promises.when(new WhenCallback<ResponseAs<JsonArray>>() {
                        @Override
                        public Deferred<ResponseAs<JsonArray>> run(final WhenParams params) throws Exception {
                            final OkHttpClient client = new OkHttpClient();
                            final Request req = new Request.Builder().url("https://raw.githubusercontent.com/loilo-inc/loilo-promise/withOkhttp/promise-samples-http/src/androidTest/assets/sample.json").get().build();
                            final Call call = client.newCall(req);
                            return new HttpTask(call).asJsonArray().progress(new ProgressReporter<HttpProgress>() {
                                @Override
                                public void report(HttpProgress httpProgress) {
                                    loader.reportProgress(new Transfer<>(params, httpProgress));
                                }
                            }).promise().get(params);
                        }
                    }).succeeded(new SuccessCallback<ResponseAs<JsonArray>, List<String>>() {
                        @Override
                        public Deferred<List<String>> run(SuccessParams<ResponseAs<JsonArray>> params) throws Exception {
                            final JsonArray array = params.getValue().getBody();
                            final List<String> list = new ArrayList<>();
                            for (JsonElement elem : array) {
                                final String str = elem.getAsString();
                                list.add(str);
                            }
                            return Defer.success(list);
                        }
                    });
                }
            });
        }

        @Override
        public void onLoadFinished(Loader<Result<List<String>>> loader, final Result<List<String>> data) {
            Dispatcher.getMainDispatcher().post(new Runnable() {
                @Override
                public void run() {
                    if(data.getCancelToken().isCanceled()){
                        return;
                    }

                    @SuppressWarnings("ThrowableResultOfMethodCallIgnored") final Exception e = data.getException();
                    if(e != null){
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    mAdapter.load(data.getValue());
                }
            });
        }

        @Override
        public void onLoaderReset(Loader<Result<List<String>>> loader) {

        }
    };

    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private Adapter mAdapter = new Adapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = (ProgressBar) findViewById(R.id.main_progress_bar);
        mRecyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerView.setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(0, Bundle.EMPTY, mLoaderCallbacks);
    }

    @Override
    protected void onStart() {
        super.onStart();

        ProgressPromiseLoader.attachProgressCallback(getSupportLoaderManager(), 0, mLoaderCallbacks);
    }

    @Override
    protected void onStop() {
        super.onStop();

        ProgressPromiseLoader.detachProgressCallback(getSupportLoaderManager(), 0, mLoaderCallbacks);
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder>{

        private List<String> mList = new ArrayList<>();

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(new TextView(MainActivity.this));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final TextView textView = (TextView) holder.itemView;
            textView.setText(mList.get(position));
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        public void load(List<String> list){
            mList = list;
            notifyDataSetChanged();
        }
    }
}
