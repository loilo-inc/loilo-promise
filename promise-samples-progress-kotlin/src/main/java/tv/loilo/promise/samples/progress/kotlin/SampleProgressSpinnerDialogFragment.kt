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

                dismiss()

                data.whenSucceeded({
                    resolveListener<OnFinishedListener>()?.onSampleProgressSpinnerSucceeded()
                }, whenFailed = {
                    resolveListener<OnFinishedListener>()?.onSampleProgressSpinnerFailed(it)
                }) ?: run {
                    resolveListener<OnFinishedListener>()?.onSampleProgressSpinnerCanceled()
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