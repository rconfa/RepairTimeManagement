package com.example.technobit.ui.signature;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.technobit.R;

public class signatureFragment extends Fragment {

    private SignatureViewModel mViewModel;
    private EditText et_description;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mViewModel =
                ViewModelProviders.of(this).get(SignatureViewModel.class);
        View root = inflater.inflate(R.layout.fragment_signature, container, false);

        et_description = (EditText) root.findViewById(R.id.et_eventDesc);


        et_description.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    hideKeyboard();
                }
            }
        });

        return root;
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_description.getWindowToken(), 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
