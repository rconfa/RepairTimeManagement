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
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.technobit.repair_timer.R;
import com.technobit.repair_timer.databinding.FragmentSignatureBinding;
import com.technobit.repair_timer.ui.customize.dialog.colorDialog.ColorUtility;
import com.technobit.repair_timer.utils.Constants;
import com.technobit.repair_timer.utils.SmartphoneControlUtility;
import com.technobit.repair_timer.utils.dataNotSent.GoogleDataSingleton;
import com.technobit.repair_timer.utils.googleService.GoogleAsyncResponse;
import com.technobit.repair_timer.utils.googleService.calendar.InsertToGoogleCalendar;
import com.technobit.repair_timer.utils.googleService.drive.InsertToGoogleDrive;

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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mBinding = FragmentSignatureBinding.inflate(inflater, container,false);
        View view = mBinding.getRoot();

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
                    // snackbar to send an Hint to the user
                    Snackbar snackbar = Snackbar.make(view, R.string.snackbar_bitmap_error, Snackbar.LENGTH_LONG);
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
                    // todo: save data if screen change portrait -> landscape (no su file notSave.txt)
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


        return view;
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
                boolean canVib = mSharedPref.getBoolean(Constants.TOOLS_SHARED_PREF_VIBRATION, true);

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
                Snackbar snackbar = Snackbar.make(requireView(), R.string.snackbar_send_positive, Snackbar.LENGTH_LONG);
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
                boolean canVib = mSharedPref.getBoolean(Constants.TOOLS_SHARED_PREF_VIBRATION, true);

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
        if(!mBinding.signatureView.isBitmapEmpty()){
            File file = new File(requireContext().getFilesDir() + "/" +
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
        File file = new File(requireContext().getFilesDir() + "/" +
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
