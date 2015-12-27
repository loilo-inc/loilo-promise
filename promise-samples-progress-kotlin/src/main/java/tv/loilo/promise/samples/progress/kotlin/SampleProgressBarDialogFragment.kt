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

package tv.loilo.promise.samples.progress.kotlin

import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.content.Loader
import android.support.v7.app.AppCompatDialogFragment
import tv.loilo.promise.Result
import tv.loilo.promise.Transfer
import tv.loilo.promise.kotlin.defer
import tv.loilo.promise.kotlin.postOnUi
import tv.loilo.promise.kotlin.promiseWhen
import tv.loilo.promise.kotlin.whenSucceeded
import tv.loilo.promise.support.ProgressPromiseLoaderCallbacks
import tv.loilo.promise.support.kotlin.attachProgressPromiseLoader
import tv.loilo.promise.support.kotlin.cancelLoader
import tv.loilo.promise.support.kotlin.createProgressPromiseLoader
import tv.loilo.promise.support.kotlin.detachProgressPromiseLoader
import java.util.concurrent.TimeUnit

/**
 * Created by pepeotoito on 2015/12/27.
 */
class SampleProgressBarDialogFragment : AppCompatDialogFragment(), ProgressPromiseLoaderCallbacks<Unit, Int> {
    companion object {
        val LOADER_ID = 0
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
        val progressDialog = ProgressDialog(context, theme)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.max = 100
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
        progressDialog?.getButton(DialogInterface.BUTTON_NEGATIVE)?.setOnClickListener {
            loaderManager.cancelLoader(LOADER_ID)
        }
    }

    override fun onResume() {
        super.onResume()

        attachProgressPromiseLoader(LOADER_ID, this)
    }

    override fun onPause() {

        detachProgressPromiseLoader(LOADER_ID, this)

        super.onPause()
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Result<Unit>>? {
        return createProgressPromiseLoader(this, { loader ->
            promiseWhen {
                defer {
                    for (i in 0..99) {
                        loader.reportProgress(Transfer(it, i))
                        TimeUnit.MILLISECONDS.sleep(100)
                    }
                    loader.reportProgress(Transfer(it, 100))
                }
            }
        })
    }

    override fun onLoaderReset(loader: Loader<Result<Unit>>?) {

    }

    override fun onLoadFinished(loader: Loader<Result<Unit>>?, data: Result<Unit>) {
        postOnUi {
            if (isResumed) {
                val listener = resolveListener<OnFinishedListener>()
                dismiss()
                if (listener != null) {
                    data.whenSucceeded({
                        listener.onSampleProgressBarSucceeded()
                    }, whenFailed = {
                        listener.onSampleProgressBarFailed(it)
                    }) ?: run {
                        listener.onSampleProgressBarCanceled()
                    }
                }
            }
        }
    }

    override fun onLoaderProgress(id: Int, progress: Int) {
        val progressDialog = dialog as? ProgressDialog
        progressDialog?.progress = progress
    }

    interface OnFinishedListener {
        fun onSampleProgressBarCanceled()
        fun onSampleProgressBarFailed(e: Exception)
        fun onSampleProgressBarSucceeded()
    }
}