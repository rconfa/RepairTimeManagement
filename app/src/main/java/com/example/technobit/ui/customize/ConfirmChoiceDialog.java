package com.example.technobit.ui.customize;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.technobit.R;

// Custom yes/no dialog and an interface to handle yes answer.
public class ConfirmChoiceDialog extends DialogFragment {

    String mTitle, mMessage; // title and message for the dialog
    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;


    public ConfirmChoiceDialog(String mTitle, String mMessage, NoticeDialogListener mListener) {
        this.mTitle = mTitle;
        this.mMessage = mMessage;
        this.mListener = mListener;
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        //public void onDialogNegativeClick(DialogFragment dialog);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog and set up the button click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if(!mTitle.equals(" ")) // if a title is defined I set it
            builder.setTitle(mTitle);
        builder.setMessage(mMessage);

        builder.setPositiveButton(R.string.dialog_btn_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Send the positive button event back to the host activity
                mListener.onDialogPositiveClick(ConfirmChoiceDialog.this);
            }
        })
        .setNegativeButton(R.string.dialog_btn_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss(); // dismiss the dialog
            }
        });

        return builder.create();
    }
}
