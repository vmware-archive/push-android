package com.gopivotal.pushlib.dialogfragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class SendMessageDialogFragment extends DialogFragment {

    public static final CharSequence[] items = new CharSequence[] {"Via GCM", "Via Back-End", "Cancel"};
    public static final int VIA_GCM = 0;
    public static final int VIA_BACK_END = 1;
    public static final int CANCELLED = 2;
    private final Listener listener;

    public interface Listener {
        void onClickResult(int result);
    }

    public SendMessageDialogFragment(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Send Message");
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (listener != null) {
                    listener.onClickResult(CANCELLED);
                }
            }
        });
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onClickResult(which);
                }
            }
        });
        return builder.create();
    }
}
