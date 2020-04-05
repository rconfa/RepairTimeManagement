package com.example.technobit.ui.customize.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.technobit.R;
import com.example.technobit.utilities.data.Contact;

// class to add a new contact or modify an existing one
public class ManageContactDialog extends DialogFragment {

    private String mTitle, mName, mEmail; // title, name and email to dispay into the dialog
    // Use this instance of the interface to deliver action events
    private NoticeDialogListener mListener;
    private int mPosition;
    private Context mContext;

    public ManageContactDialog(String mTitle, String mName, String mEmail, int position,
                               Context mContext, NoticeDialogListener mListener) {
        this.mTitle = mTitle;
        this.mName = mName;
        this.mEmail = mEmail;
        this.mPosition = position;
        this.mContext = mContext;
        this.mListener = mListener;
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        // c = new contact
        // position = if the contact is already existing is the position to modify
        void onDialogPositiveClick(Contact c, int position);
        //public void onDialogNegativeClick(DialogFragment dialog);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        // get the layout inflater and set the view
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.custom_dialog_add_contact,null );
        builder.setView(v);

        // get layout items
        final Button yes = v.findViewById(R.id.btn_yes);
        final Button no = v.findViewById(R.id.btn_no);
        final TextView etTitle =  v.findViewById(R.id.et_dialog_title);
        final EditText etName = v.findViewById(R.id.et_dialog_name);
        final EditText etEmail = v.findViewById(R.id.et_dialog_email);
        // setting values
        etTitle.setText(mTitle);
        if(!mName.isEmpty())
            etName.setText(mName);
        if(!mEmail.isEmpty())
            etEmail.setText(mEmail);

        // if position != -1 it's an update dialog so the button yes must be visibile
        if(mPosition!=-1)
            yes.setVisibility(View.VISIBLE);

        // Set button yes enable only if the name is not empty
        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count!=0) // if the name is not empty set the yes but to visible
                    yes.setVisibility(View.VISIBLE);
                else
                    yes.setVisibility(View.INVISIBLE);
            }

        });

        // If click on yes I return
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get values
                String name = etName.getText().toString();
                String email = etEmail.getText().toString();
                // change values
                Contact contact = new Contact(name, email);
                // Send the positive button event back to the host activity
                mListener.onDialogPositiveClick(contact,mPosition);
                dismiss(); // close the dialog
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss(); // close the dialog
            }
        });

        return builder.create();
    }
}