package com.example.technobit.ui.signature;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import androidx.preference.PreferenceManager;

import com.example.technobit.R;
import com.example.technobit.ui.customize.signatureview.SignatureView;
import com.example.technobit.utilities.SmartphoneControlUtility;
import com.example.technobit.utilities.googleService.GoogleAsyncResponse;
import com.example.technobit.utilities.googleService.calendar.AsyncInsertGoogleCalendar;
import com.google.android.material.snackbar.Snackbar;

public class SignatureFragment extends Fragment implements GoogleAsyncResponse {

    private SignatureViewModel mViewModel;
    private EditText et_description;
    private Button btn_send;
    private Button btn_clear;
    private SignatureView signatureView;
    private SharedPreferences sharedPref;
    private GoogleAsyncResponse Asyncdelegate;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mViewModel = ViewModelProviders.of(this).get(SignatureViewModel.class);

        // view of the layout
        View root = inflater.inflate(R.layout.fragment_signature, container, false);

        Bundle bundle = this.getArguments();
        // get bundle values
        final long endMillis = bundle.getLong("endMillis", -1);
        final long startMillis =  bundle.getLong("startMillis", -1);
        final String eventTitle = bundle.getString("EventTitle", "");

        // Shared preference for get/set all the preference
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        
        
        // get all UI object
        // Button to clear the signature
        btn_clear = (Button) root.findViewById(R.id.btn_clear);
        // Button for send all data to calendar
        btn_send  = (Button) root.findViewById(R.id.btn_send);
        // edittext for event description
        et_description = (EditText) root.findViewById(R.id.et_eventDesc);
        // signatureView for client signature
        signatureView = root.findViewById(R.id.signature_view);

        // Event on click on button "clear"
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearCanvas();
            }
        });

        Asyncdelegate = this; // set my Asyncdelegate equal to my method implemented in this class

        // Event on click on button "send"
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String desc = et_description.getText().toString();
                String email = sharedPref.getString(getString(R.string.shared_email), null);
                int color = getColorInt();
                AsyncInsertGoogleCalendar gCal = new AsyncInsertGoogleCalendar(eventTitle, desc,
                        startMillis, endMillis, color, getContext(), Asyncdelegate);
                gCal.execute();
            }

        });


        // If the editText no longer as the focus I close the keyboard
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

    // Close the keyboard if open
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_description.getWindowToken(), 0);
    }

    // Clear the canvas
    private void clearCanvas(){
        signatureView.clearCanvas();
    }

    // get the signature image
    private Bitmap getImage(){
        return signatureView.getSignatureBitmap();
    }


    private int getColorInt(){
        String def_color = getResources().getString(R.string.default_color_str);
        int defaultColorValue = Color.parseColor(def_color);

        // Get the selected color from the shared preference
        int color_selected = sharedPref.getInt(getString(R.string.shared_saved_color), defaultColorValue);

        int[] mColorChoices=null;
        String[] color_array = getResources().getStringArray(R.array.default_color_choice_values);

        int colorVal = -1;
        if (color_array!=null && color_array.length>0) {
            int i = 0;
            while(i < color_array.length || colorVal == -1){
                if(Color.parseColor(color_array[i]) == color_selected)
                    colorVal = ++i;
                ++i;
            }
        }
        // If I don't find the color return 1 (default color) else the right color val
        return colorVal == -1 ?  1 : colorVal;
    }


    @Override
    public void processFinish(String result) {
        if(result == null) {
            // TODO: save data, show snackbar before back pressed
            SmartphoneControlUtility scu = new SmartphoneControlUtility(getContext(), true);
            scu.shake();
            getActivity().onBackPressed();
        }
        else{
            Snackbar snackbar = Snackbar.make(getView(), R.string.snackbar_send_positive, Snackbar.LENGTH_LONG);
            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary))
                    .setAction(getString(R.string.snackbar_close_btn), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    });
            snackbar.show();
            getActivity().onBackPressed();
        }
    }
}
