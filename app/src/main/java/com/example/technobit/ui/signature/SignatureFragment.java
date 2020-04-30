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
import androidx.preference.PreferenceManager;

import com.example.technobit.R;
import com.example.technobit.ui.customize.dialog.colorDialog.ColorUtility;
import com.example.technobit.ui.customize.signatureview.SignatureView;
import com.example.technobit.utilities.SmartphoneControlUtility;
import com.example.technobit.utilities.googleService.GoogleAsyncResponse;
import com.example.technobit.utilities.googleService.calendar.InsertToGoogleCalendar;
import com.example.technobit.utilities.googleService.drive.InsertToGoogleDrive;
import com.example.technobit.utilities.notSendedData.GoogleDataSingleton;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

public class SignatureFragment extends Fragment {

    private SignatureViewModel mViewModel;
    private EditText mEditTextDescription;
    private SignatureView mSignatureView;
    private SharedPreferences mSharedPref;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

       // mViewModel = ViewModelProviders.of(this).get(SignatureViewModel.class);

        // view of the layout
        View root = inflater.inflate(R.layout.fragment_signature, container, false);

        // Shared preference for get/set all the preference
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        
        // get all UI object
        // Button to clear the signature
        Button mBtnClear = root.findViewById(R.id.btn_clear);
        // Button for send all data to calendar
        final Button mBtnSend = root.findViewById(R.id.btn_send);
        // edit text for event description
        mEditTextDescription = root.findViewById(R.id.et_eventDesc);
        // signatureView for client signature
        mSignatureView = root.findViewById(R.id.signature_view);

        // Event on click on button "clear"
        mBtnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearCanvas(); // clear the canvas
            }
        });


        // Event on click on button "send"
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check if the user has insert a sign
                if(mSignatureView.isBitmapEmpty()){
                    // snackbar to send an Hint to the user
                    Snackbar snackbar = Snackbar.make(getView(), R.string.snackbar_start_error, Snackbar.LENGTH_LONG);
                    snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary))
                            .setAction(getString(R.string.snackbar_close_btn), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                }
                            });
                    snackbar.show();
                }
                else {
                    // set the event description
                    GoogleDataSingleton.getData().setDescription(mEditTextDescription.getText().toString());

                    saveAllOnGoogle();
                }
            }
        });


        // If the editText no longer as the focus I close the keyboard
        mEditTextDescription.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
        if (imm != null) {
            imm.hideSoftInputFromWindow(mEditTextDescription.getWindowToken(), 0);
        }
    }

    // Clear the canvas
    private void clearCanvas(){
        mSignatureView.clearCanvas();
    }

    // get the signature image
    private Bitmap getImage(){
        return mSignatureView.getSignatureBitmap();
    }


    private int getColorInt(){
        String defColor = getResources().getString(R.string.default_color_str);
        int defaultColorValue = Color.parseColor(defColor);
        // Get the selected color from the shared preference
        int colorSelected = mSharedPref.getInt(getString(R.string.shared_saved_color), defaultColorValue);

        // get all color list defined
        ColorUtility colorUtility = new ColorUtility(getContext());
        ArrayList<Integer> mColor = colorUtility.getColorsArrayList();
        return mColor.indexOf(colorSelected);

    }

    // First upload the image on drive, on result add the event with the attachment on calendar
    private void saveAllOnGoogle() {
        File file = writeBitmapOnFile();
        if(file != null){
            GoogleDataSingleton.getData().setImage(file.getPath());
            GoogleDataSingleton.getData().setCase(2);

            new InsertToGoogleDrive(GoogleDataSingleton.getData().getEventTitle(),
                    file, getContext(),mDriveResponse).start();


        }
        // todo: else
    }

    // Implement the interface to handle the asyncTask response for google drive uploading
    private GoogleAsyncResponse mDriveResponse = new GoogleAsyncResponse(){
        @Override
        public void processFinish(String attachment) {
            if (!attachment.equals("false")) {
                // setting the attachment and the case in singleton
                GoogleDataSingleton.getData().setImage(attachment);
                GoogleDataSingleton.getData().setCase(1);

                // delete the bitmap file, is useless now
                deleteBitmapFile();
                sendToCalendar();
            }
            else {
                // check if the user let vibrate the smartphone
                boolean canVib = mSharedPref.getBoolean(getString(R.string.shared_vibration), true);

                if(canVib)
                    new SmartphoneControlUtility(getContext()).shake(); // shake smartphone

                // go back to the precedent activity
                getParentFragmentManager().popBackStack();
            }
        }
    };

    private void sendToCalendar(){
        // when the image is upload I add the event on calendar
        int color = getColorInt(); // get the color that the user has choose
        // insert the event on calendar
        Date endDate = new Date(GoogleDataSingleton.getData().getEventEnd());


        new InsertToGoogleCalendar(GoogleDataSingleton.getData().getEventTitle(),
                GoogleDataSingleton.getData().getDescription(), endDate,
                GoogleDataSingleton.getData().getEventDuration(), color, getContext(),
                GoogleDataSingleton.getData().getImage(),mCalendarResponse).start();
    }

    // Implement the interface to handle the asyncTask response for google calendar insert
    private GoogleAsyncResponse mCalendarResponse = new GoogleAsyncResponse(){
        @Override
        public void processFinish(String result) {
            // if result == null the event is no added on google
            if(result.equals("true")) {
                GoogleDataSingleton.reset(); // it all sent, I reset the value to null

                // create a snackbar with a positive message
                Snackbar snackbar = Snackbar.make(getView(), R.string.snackbar_send_positive, Snackbar.LENGTH_LONG);
                snackbar.setTextColor(Color.WHITE);
                snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary))
                        .setAction(getString(R.string.snackbar_close_btn), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                            }
                        });
                snackbar.show();

                // go back to the precedent activity
                getParentFragmentManager().popBackStack();
            }
            else{
                // check if the user let vibrate the smartphone
                boolean canVib = mSharedPref.getBoolean(getString(R.string.shared_vibration), true);

                if(canVib)
                    new SmartphoneControlUtility(getContext()).shake(); // shake smartphone

                // go back to the precedent activity
                getParentFragmentManager().popBackStack();
            }
        }
    };

    // Write the bitmap on file.
    // return the file on success, null on error
    private File writeBitmapOnFile(){
        // check if the user as insert his sign
        if(!mSignatureView.isBitmapEmpty()){
            File file = new File(getContext().getFilesDir() + "/" +
                    GoogleDataSingleton.getData().getEventTitle() +".jpeg");
            OutputStream os;
            try {
                os = new BufferedOutputStream(new FileOutputStream(file));
                Bitmap b = getImage();
                // compress the bitmap and save it to file
                b.compress(Bitmap.CompressFormat.JPEG, 100, os);
                os.flush();
                os.close();
                return file;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return null;
    }

    private void deleteBitmapFile(){
        File file = new File(getContext().getFilesDir() + "/" +
                GoogleDataSingleton.getData().getEventTitle() + ".jpeg");
        file.delete();
    }

    @Override
    // if the user destroy this fragment without send the info on google I save all into file
    public void onDestroy() {
        if(GoogleDataSingleton.isInstanceNull()) { // if the instance is not null I save it.
            try {
                GoogleDataSingleton.saveInstance(getContext());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
}
