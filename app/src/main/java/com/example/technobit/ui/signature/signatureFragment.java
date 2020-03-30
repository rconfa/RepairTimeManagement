package com.example.technobit.ui.signature;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.technobit.R;
import com.example.technobit.ui.signature.signatureview.SignatureView;

public class signatureFragment extends Fragment {

    private SignatureViewModel mViewModel;
    private EditText et_description;
    private Button btn_send;
    private Button btn_clear;
    private SignatureView signatureView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mViewModel = ViewModelProviders.of(this).get(SignatureViewModel.class);

        View root = inflater.inflate(R.layout.fragment_signature, container, false);

        // prendo tutti gli oggetti ui necessari
        // bottone per pulire la firma
        btn_clear = (Button) root.findViewById(R.id.btn_clear);
        // bottone per inviare tutti i dati!
        btn_send  = (Button) root.findViewById(R.id.btn_send);
        // edit text con la descrizione
        et_description = (EditText) root.findViewById(R.id.et_eventDesc);
        // signature View per la firma del cliente
        signatureView = (SignatureView) root.findViewById(R.id.signature_view);

        // evento sul click per pulire la firma
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearCanvas();
            }
        });

        // evento sul click per inviare tutto
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendOnCalendar();
            }
        });


        // se cambia il focus chiudo la tastiera
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

    // se la tastiera è aperta la chiudo!
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_description.getWindowToken(), 0);
    }

    // pulisco tutto il canvas
    private void clearCanvas(){
        signatureView.clearCanvas();
    }

    // ottengo l'immagine
    private Bitmap getImage(){
        return signatureView.getSignatureBitmap();
    }

    private void sendOnCalendar(){
        // TODO send to calendar

        /*
        if(signatureView.isBitmapEmpty() == false){
            String path = getContext().getFilesDir() +  "/testBitmap.JPEG";
            Bitmap b = getImage(); // prendo l'immagine
            // converto la bitmap in jpg e aggiungo tutto al calendario google
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            b.compress(Bitmap.CompressFormat.JPEG, 100, out);

            new upload().execute();


        }*/

    }


}
