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

package tv.loilo.promise.samples.progress.kotlin

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment

/**
 * Created by pepeotoito on 2015/12/27.
 */
class MessageDialogFragment : AppCompatDialogFragment() {
    companion object {
        private val MESSAGE_TAG = "message"
        fun newInstance(message: String): MessageDialogFragment {
            return MessageDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(MESSAGE_TAG, message)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //TODO arguments!!でいいのか？
        val message = arguments!!.getString(MESSAGE_TAG)
        //TODO context!!でいいのか？
        return AlertDialog.Builder(context!!, theme)
                .setMessage(message)
                .setPositiveButton("Close", { _, _ ->

                }).create()
    }
}