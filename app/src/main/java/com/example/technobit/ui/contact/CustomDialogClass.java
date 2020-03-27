package com.example.technobit.ui.contact;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.technobit.R;

public class CustomDialogClass extends Dialog implements android.view.View.OnClickListener {

    private Activity c;
    private Dialog d;
    private Button yes, no;
    private EditText et_name;

    public CustomDialogClass(Activity activity) {
        super(activity);
        // TODO Auto-generated constructor stub
        this.c = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_dialog_add_contact);
        yes = (Button) findViewById(R.id.btn_yes);
        no = (Button) findViewById(R.id.btn_no);
        et_name = (EditText) findViewById(R.id.et_dialog_name);
        et_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count!=0) // se ha scritto il nome attivo il bottone ok
                    yes.setVisibility(View.VISIBLE);
                else
                    yes.setVisibility(View.INVISIBLE);
            }

        });

        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:
                //todo: come restituisco i dati
                // dismiss();
                c.finish(); // TODO: cosi si chiude tutta l'app, sistemare
                break;
            case R.id.btn_no:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }

}