/*
 * Copyright 2016 LoiLo inc.
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

package tv.loilo.promise.samples.errorhandling

import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.Loader
import tv.loilo.promise.Result
import tv.loilo.promise.kotlin.defer
import tv.loilo.promise.kotlin.postOnUi
import tv.loilo.promise.kotlin.promiseWhen
import tv.loilo.promise.support.PromiseLoaderCallbacks
import tv.loilo.promise.support.kotlin.createPromiseLoader

/**
 * Created by pepeotoito on 2016/01/02.
 */
class ErrorOnCrashDialogFragment : DialogFragment(), PromiseLoaderCallbacks<Unit> {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val progressDialog = ProgressDialog(context)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setMessage("Loading...")
        return progressDialog
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog.setCancelable(false)

        loaderManager.initLoader(0, Bundle.EMPTY, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Result<Unit>>? {
        return createPromiseLoader(context, {
            promiseWhen {
                defer<Unit> {
                    throw Error("Throw test error.(Crash expected.)")
                }
            }
        })
    }

    override fun onLoadFinished(loader: Loader<Result<Unit>>?, data: Result<Unit>?) {
        postOnUi {
            if (isResumed) {
                dismiss()
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Result<Unit>>?) {

    }
}