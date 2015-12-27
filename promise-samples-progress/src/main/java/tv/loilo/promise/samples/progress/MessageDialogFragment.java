package tv.loilo.promise.samples.progress;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

/**
 * Created by pepeotoito on 2015/12/26.
 */
public class MessageDialogFragment extends AppCompatDialogFragment {

    private static final String MESSAGE_TAG = "message";

    public static MessageDialogFragment newInstance(String message) {
        final MessageDialogFragment fragment = new MessageDialogFragment();
        final Bundle bundle = new Bundle();
        bundle.putString(MESSAGE_TAG, message);
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String message = getArguments().getString(MESSAGE_TAG);

        return new AlertDialog.Builder(getContext(), getTheme())
                .setMessage(message)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
    }
}
