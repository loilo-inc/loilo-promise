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
import tv.loilo.promise.EntryFunction;
import tv.loilo.promise.EntryParams;
import tv.loilo.promise.Promise;
import tv.loilo.promise.Promises;
import tv.loilo.promise.Result;
import tv.loilo.promise.support.PromiseLoader;
import tv.loilo.promise.support.PromiseLoaderCallbacks;

/**
 * Created by pepeotoito on 2015/12/26.
 */
public class SampleProgressSpinnerDialogFragment extends AppCompatDialogFragment implements PromiseLoaderCallbacks<Void> {
    private static final int LOADER_ID = 0;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ProgressDialog progressDialog = new ProgressDialog(getContext(), getTheme());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
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
                PromiseLoader.cancelLoader(getLoaderManager(), LOADER_ID);
            }
        });

    }

    @Override
    public Loader<Result<Void>> onCreateLoader(int id, Bundle args) {
        return new PromiseLoader<Void>(getContext()) {
            @NonNull
            @Override
            protected Promise<Void> onCreatePromise() throws Exception {
                return Promises.when(new EntryFunction<Void>() {
                    @Override
                    public Deferred<Void> run(EntryParams params) throws Exception {
                        TimeUnit.SECONDS.sleep(10);
                        return Defer.success(null);
                    }
                });
            }
        };
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
                            listener.onSampleProgressSpinnerCanceled();
                            return;
                        }

                        final Exception e = data.getException();
                        if (e != null) {
                            listener.onSampleProgressSpinnerFailed(e);
                        } else {
                            listener.onSampleProgressSpinnerSucceeded();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Result<Void>> loader) {

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

        void onSampleProgressSpinnerCanceled();

        void onSampleProgressSpinnerFailed(@NonNull Exception e);

        void onSampleProgressSpinnerSucceeded();
    }
}
