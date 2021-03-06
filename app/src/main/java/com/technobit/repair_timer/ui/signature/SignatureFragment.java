package com.technobit.repair_timer.ui.signature;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.technobit.repair_timer.R;
import com.technobit.repair_timer.databinding.FragmentSignatureBinding;
import com.technobit.repair_timer.repositories.dataNotSent.GoogleDataSingleton;
import com.technobit.repair_timer.service.google.GoogleAsyncResponse;
import com.technobit.repair_timer.service.google.calendar.InsertToGoogleCalendar;
import com.technobit.repair_timer.service.google.drive.InsertToGoogleDrive;
import com.technobit.repair_timer.service.google.gmail.SendEmail;
import com.technobit.repair_timer.ui.customize.dialog.colorDialog.ColorUtility;
import com.technobit.repair_timer.utils.Constants;
import com.technobit.repair_timer.utils.SmartphoneControlUtility;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

public class SignatureFragment extends Fragment {

    private FragmentSignatureBinding mBinding;
    private SharedPreferences mSharedPref;
    private String mStringDate;

    public SignatureFragment() {
        // Required empty public constructor
    }

    public static SignatureFragment newInstance() {
        return new SignatureFragment();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mBinding = FragmentSignatureBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            GoogleDataSingleton.StringInitialize(savedInstanceState.getString(Constants.SAVE_INSTANCE_GOOGLE));
        }

        // Shared preference for get/set all the preference
        mSharedPref = requireContext().getSharedPreferences(
                Constants.TOOLS_SHARED_PREF_FILENAME, Context.MODE_PRIVATE);


        // Event on click on button "clear"
        mBinding.btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearCanvas(); // clear the canvas
            }
        });


        // Event on click on button "send"
        mBinding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check if the user has insert a sign
                if(mBinding.signatureView.isBitmapEmpty()){
                    displaySnackbar(R.string.snackbar_bitmap_error);
                }
                else {
                    // set the event description
                    GoogleDataSingleton.getData().setDescription(mBinding.etEventDesc.getText().toString());
                    mBinding.signatureView.setEnableSignature(false);
                    mBinding.signatureView.setEnabled(false);
                    mBinding.etEventDesc.setEnabled(false);
                    saveAllOnGoogle();
                }
            }
        });


        // If the editText no longer as the focus I close the keyboard
        mBinding.etEventDesc.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    hideKeyboard();
                }
            }
        });
    }


    // Close the keyboard if open
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mBinding.etEventDesc.getWindowToken(), 0);
        }
    }

    // Clear the canvas
    private void clearCanvas(){
        mBinding.signatureView.clearCanvas();
    }

    // get the signature image
    private Bitmap getImage(){
        return mBinding.signatureView.getSignatureBitmap();
    }


    private int getColorInt(){
        int defaultColorValue = Color.parseColor(Constants.default_color);
        // Get the selected color from the shared preference
        int colorSelected = mSharedPref.getInt(Constants.TOOLS_SHARED_PREF_GOOGLE_COLOR, defaultColorValue);

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

            new InsertToGoogleDrive(file, getContext(),mDriveResponse).start();
        }
        else{
            GoogleDataSingleton.getData().setImage(null);
            GoogleDataSingleton.getData().setCase(3);
            sendToCalendar();
        }
    }

    // Implement the interface to handle the asyncTask response for google drive uploading
    private GoogleAsyncResponse mDriveResponse = new GoogleAsyncResponse(){
        @Override
        public void processFinish(String attachment) {
            if(attachment.equals("noInternet") || attachment.equals("false")){
                // check if the user let vibrate the smartphone
                boolean canVib = mSharedPref.getBoolean(Constants.TOOLS_SHARED_PREF_VIBRATION, true);

                if(canVib)
                    new SmartphoneControlUtility(getContext()).shake(); // shake smartphone

                displaySnackbar(R.string.snackbar_no_internet);

                // go back to the precedent activity
                safe_press_back();
            }
            else {
                // path of the file to delete
                String filepath = GoogleDataSingleton.getData().getImage();
                // setting the attachment and the case in singleton
                GoogleDataSingleton.getData().setImage(attachment);
                GoogleDataSingleton.getData().setCase(1);

                // delete the bitmap file, is useless now
                deleteBitmapFile(filepath);
                sendToCalendar();
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
                // Event is added
                GoogleDataSingleton.getData().setCase(4); // change the case
                // delete unused information
                GoogleDataSingleton.getData().setImage(null);
                GoogleDataSingleton.getData().setEventTitle(null);
                GoogleDataSingleton.getData().setEventDuration(null);
                sendEmail(); // try to send email
            }
            else{
                // check if the user let vibrate the smartphone
                boolean canVib = mSharedPref.getBoolean(Constants.TOOLS_SHARED_PREF_VIBRATION, true);

                if(canVib)
                    new SmartphoneControlUtility(getContext()).shake(); // shake smartphone

                displaySnackbar(R.string.snackbar_no_internet);

                // go back to the precedent activity
                safe_press_back();
            }
        }
    };

    private void sendEmail(){
        String subj = getString(R.string.emailSubject) + " " + mStringDate;

        // boolean b = new SmartphoneControlUtility(getContext()).emailIsValid("r");

        new SendEmail(getContext(), GoogleDataSingleton.getData().getEmail(), subj,
                GoogleDataSingleton.getData().getDescription(), mEmailSender).start();
    }

    private GoogleAsyncResponse mEmailSender = new GoogleAsyncResponse(){
        @Override
        public void processFinish(String result) {
            // if result == true the email was sent
            if(result.equals("true")) {
                GoogleDataSingleton.reset(); // it all sent, I reset the value to null
                displaySnackbar(R.string.snackbar_send_positive);

                // go back to the precedent activity
                safe_press_back();
            }
            else{
                // check if the user let vibrate the smartphone
                boolean canVib = mSharedPref.getBoolean(Constants.TOOLS_SHARED_PREF_VIBRATION, true);

                if(canVib)
                    new SmartphoneControlUtility(getContext()).shake(); // shake smartphone

                displaySnackbar(R.string.snackbar_no_internet);

                // go back to the precedent activity
                safe_press_back();
            }
        }
    };

    // Write the bitmap on file.
    // return the file on success, null on error
    private File writeBitmapOnFile(){
        // check if the user as insert his sign
        if(!mBinding.signatureView.isBitmapEmpty()){
            // get the date of finishing
            Date endDate = new Date(GoogleDataSingleton.getData().getEventEnd());
            mStringDate = (String) android.text.format.DateFormat.format("dd-MM-yyyy HH:mm:ss", endDate);
            File file = new File(requireContext().getFilesDir() + "/" +
                    GoogleDataSingleton.getData().getEventTitle() + "_" + mStringDate
                    + ".jpeg");
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

    private void deleteBitmapFile(String path){
        File file = new File(path);
        file.delete();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(Constants.SAVE_INSTANCE_GOOGLE, GoogleDataSingleton.getData().toString());
        GoogleDataSingleton.reset();
    }

    @Override
    public void onDestroyView() {
        // if there is some data not sent to google I save it
        if(GoogleDataSingleton.isInstanceNull()) { // if the instance is not null I save it.
            try {
                GoogleDataSingleton.saveInstance(getContext());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.onDestroyView();
    }


    // go back to the previews fragment and saved the instance of the event that is not sent
    private void safe_press_back(){
        // check if the fragment is available
        if(this.isAdded()) {
            // Go back to the precedent activity
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    requireActivity().onBackPressed();
                }
            });
        }
    }

    private void displaySnackbar(int stringId){
        // create a snackbar with a positive message
        Snackbar snackbar = Snackbar.make(requireView(), stringId, Snackbar.LENGTH_LONG);
        snackbar.setTextColor(Color.WHITE);
        snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary))
                .setAction(getString(R.string.snackbar_close_btn), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });
        snackbar.show();
    }

}
