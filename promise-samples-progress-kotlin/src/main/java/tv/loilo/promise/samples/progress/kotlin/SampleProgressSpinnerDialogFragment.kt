package tv.loilo.promise.samples.progress.kotlin

import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.content.Loader
import android.support.v7.app.AppCompatDialogFragment
import tv.loilo.promise.Result
import tv.loilo.promise.kotlin.defer
import tv.loilo.promise.kotlin.postOnUi
import tv.loilo.promise.kotlin.promiseWhen
import tv.loilo.promise.kotlin.whenSucceeded
import tv.loilo.promise.support.PromiseLoaderCallbacks
import tv.loilo.promise.support.kotlin.cancelLoader
import tv.loilo.promise.support.kotlin.createPromiseLoader
import java.util.concurrent.TimeUnit

/**
 * Created by pepeotoito on 2015/12/27.
 */
class SampleProgressSpinnerDialogFragment : AppCompatDialogFragment(), PromiseLoaderCallbacks<Unit> {
    companion object {
        val LOADER_ID = 0
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
        val progressDialog = ProgressDialog(context, theme)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setMessage("Loading...")
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", { dialogInterface, i ->

        })
        return progressDialog
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog.setCancelable(false)
        loaderManager.initLoader(LOADER_ID, Bundle.EMPTY, this)
    }

    override fun onStart() {
        super.onStart()

        val progressDialog = dialog as? ProgressDialog
        progressDialog?.getButton(DialogInterface.BUTTON_NEGATIVE)?.setOnClickListener { view ->
            loaderManager.cancelLoader(LOADER_ID)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Result<Unit>>? {
        return createPromiseLoader(context, {
            promiseWhen {
                defer {
                    TimeUnit.SECONDS.sleep(10)
                }
            }
        })
    }

    override fun onLoadFinished(loader: Loader<Result<Unit>>?, data: Result<Unit>) {
        postOnUi {
            if (isResumed) {
                val listener = resolveListener<OnFinishedListener>()
                dismiss()
                if (listener != null) {
                    data.whenSucceeded({
                        listener.onSampleProgressSpinnerSucceeded()
                    }, whenFailed = {
                        listener.onSampleProgressSpinnerFailed(it)
                    }) ?: run {
                        listener.onSampleProgressSpinnerCanceled()
                    }
                }
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Result<Unit>>?) {

    }

    interface OnFinishedListener {
        fun onSampleProgressSpinnerCanceled()
        fun onSampleProgressSpinnerFailed(e: Exception)
        fun onSampleProgressSpinnerSucceeded()
    }
}