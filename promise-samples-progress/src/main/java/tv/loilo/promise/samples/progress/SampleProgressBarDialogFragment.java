/*
 * Copyright 2015 LoiLo inc.
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

package tv.loilo.promise.samples.progress;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.View;

import java.util.concurrent.TimeUnit;

import tv.loilo.promise.Defer;
import tv.loilo.promise.Deferred;
import tv.loilo.promise.Dispatcher;
import tv.loilo.promise.Promise;
import tv.loilo.promise.Promises;
import tv.loilo.promise.Result;
import tv.loilo.promise.Transfer;
import tv.loilo.promise.WhenCallback;
import tv.loilo.promise.WhenParams;
import tv.loilo.promise.support.ProgressPromiseFactory;
import tv.loilo.promise.support.ProgressPromiseLoader;
import tv.loilo.promise.support.ProgressPromiseLoaderCallbacks;

/**
 * Created by pepeotoito on 2015/12/26.
 */
public class SampleProgressBarDialogFragment extends AppCompatDialogFragment implements ProgressPromiseLoaderCallbacks<Void, Integer> {

    private static final int LOADER_ID = 0;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ProgressDialog progressDialog = new ProgressDialog(getContext(), getTheme());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setMessage("Loading...");
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        return progressDialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getDialog().setCancelable(false);
        getLoaderManager().initLoader(LOADER_ID, Bundle.EMPTY, this);
    }

    @Override
    public void onStart() {
        super.onStart();

        final ProgressDialog progressDialog = (ProgressDialog) getDialog();
        progressDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressPromiseLoader.cancelLoader(getLoaderManager(), LOADER_ID);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        ProgressPromiseLoader.attachLoader(LOADER_ID, this);
    }

    @Override
    public void onPause() {
        ProgressPromiseLoader.detachLoader(LOADER_ID, this);

        super.onPause();
    }

    @Override
    public Loader<Result<Void>> onCreateLoader(int id, Bundle args) {
        return ProgressPromiseLoader.createLoader(this, new ProgressPromiseFactory<SampleProgressBarDialogFragment, Void, Integer>() {
            @NonNull
            @Override
            public Promise<Void> createPromise(@NonNull final ProgressPromiseLoader<SampleProgressBarDialogFragment, Void, Integer> loader) {
                return Promises.when(new WhenCallback<Void>() {
                    @Override
                    public Deferred<Void> run(WhenParams params) throws Exception {

                        for (int i = 0; i < 100; ++i) {
                            loader.reportProgress(new Transfer<>(params, i));
                            TimeUnit.MILLISECONDS.sleep(100);
                        }

                        loader.reportProgress(new Transfer<>(params, 100));

                        return Defer.success(null);
                    }
                });
            }
        });
    }

    @Override
    public void onLoadFinished(Loader<Result<Void>> loader, final Result<Void> data) {
        Dispatcher.getMainDispatcher().post(new Runnable() {
            @Override
            public void run() {

                if (isResumed()) {
                    final OnFinishedListener listener = resolveListener();

                    dismiss();

                    if (listener != null) {
                        if (data.getCancelToken().isCanceled()) {
                            listener.onSampleProgressBarCanceled();
                            return;
                        }

                        final Exception e = data.getException();
                        if (e != null) {
                            listener.onSampleProgressBarFailed(e);
                        } else {
                            listener.onSampleProgressBarSucceeded();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Result<Void>> loader) {

    }

    @Override
    public void onLoaderProgress(int id, @NonNull Integer progress) {

        final ProgressDialog progressDialog = (ProgressDialog) getDialog();
        progressDialog.setProgress(progress);
    }

    private OnFinishedListener resolveListener() {
        final Fragment parentFragment = getParentFragment();
        if (parentFragment != null && parentFragment instanceof OnFinishedListener) {
            return (OnFinishedListener) parentFragment;
        }

        final FragmentActivity activity = getActivity();
        if (activity != null && activity instanceof OnFinishedListener) {
            return (OnFinishedListener) activity;
        }
        return null;
    }

    public interface OnFinishedListener {

        void onSampleProgressBarCanceled();

        void onSampleProgressBarFailed(@NonNull Exception e);

        void onSampleProgressBarSucceeded();
    }
}
