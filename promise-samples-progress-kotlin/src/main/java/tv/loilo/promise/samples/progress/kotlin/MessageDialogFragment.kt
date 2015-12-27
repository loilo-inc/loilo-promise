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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
        val message = arguments.getString(MESSAGE_TAG)
        return AlertDialog.Builder(context, theme)
                .setMessage(message)
                .setPositiveButton("Close", { dialogInterface, i ->

                }).create()
    }
}