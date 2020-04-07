package com.example.technobit.ui.send;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.example.technobit.R;
import com.example.technobit.ui.customize.dialog.colorDialog.ColorUtility;
import com.example.technobit.utilities.SmartphoneControlUtility;
import com.example.technobit.utilities.googleService.GoogleAsyncResponse;
import com.example.technobit.utilities.googleService.calendar.AsyncInsertGoogleCalendar;
import com.example.technobit.utilities.googleService.drive.AsyncInsertGoogleDrive;
import com.example.technobit.utilities.notSendedData.DataToSend;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class SendFragment extends Fragment {

    private SendViewModel sendViewModel;
    private SharedPreferences mSharedPref;
    private String mEventTitle, mDescription;
    private long mEndDate,mDuration;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //sendViewModel = ViewModelProviders.of(this).get(SendViewModel.class);
        View root = inflater.inflate(R.layout.fragment_send, container, false);
        final TextView textView = root.findViewById(R.id.text_send);

        // Shared preference for get/set all the preference
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        ArrayList<DataToSend> allData;
        try {
            allData = new DataToSend().getAll(getContext());
        } catch (IOException e) {
            allData = null;
        }

        if(allData!=null) {
            textView.setText("find " + allData.size() + " event not sended yet");
            SendAllToGoogle(allData);
        }
        else
            textView.setText("Error on file reading");


        // View root = inflater.inflate(R.layout.fragment_signature, container, false);
        return root;
    }

    private void SendAllToGoogle(ArrayList<DataToSend> allData){
        for(DataToSend singleData : allData){
            this.mEventTitle = singleData.getEventTitle();
            this.mDescription = singleData.getDescription();
            this.mDuration = singleData.getEventDuration();
            this.mEndDate = singleData.getEventEnd();

            if(singleData.getIsDriveSent().equals("true")){
                // when the image is upload I add the event on calendar
                int color = getColorInt(); // get the color that the user has choose
                // insert the event on calendar
                Date endDate = new Date(mEndDate);

                AsyncInsertGoogleCalendar gCal = new AsyncInsertGoogleCalendar(mEventTitle, mDescription,
                        endDate, mDuration, color, getContext(), mCalendarResponse, singleData.getImageData());
                gCal.execute();

                // if true I need to send only the information to the calendar
            }
            else{ // before send to drive
                File f = new File(singleData.getImageData());
                // Insert the image on google drive
                AsyncInsertGoogleDrive gDrive = new AsyncInsertGoogleDrive(singleData.getEventTitle(),
                        f, getContext(), mDriveResponse);
                gDrive.execute();
            }
        }
    }


    // Implement the interface to handle the asyncTask response for google drive uploading
    private GoogleAsyncResponse mDriveResponse = new GoogleAsyncResponse(){
        @Override
        public void processFinish(String attachment) {
            if(attachment != null) {
                deleteBitmapFile();
                // when the image is upload I add the event on calendar
                int color = getColorInt(); // get the color that the user has choose
                // insert the event on calendar
                Date endDate = new Date(mEndDate);

                AsyncInsertGoogleCalendar gCal = new AsyncInsertGoogleCalendar(mEventTitle, mDescription,
                        endDate, mDuration, color, getContext(), mCalendarResponse, attachment);
                gCal.execute();
            }
            else {
                 new SmartphoneControlUtility(getContext()).shake(); // shake smartphone
            }
        }
    };

    // Implement the interface to handle the asyncTask response for google calendar insert
    private GoogleAsyncResponse mCalendarResponse = new GoogleAsyncResponse(){
        @Override
        public void processFinish(String result) {
            // if result == null the event is no added on google
            if(!result.equals("true")) {
                 new SmartphoneControlUtility(getContext()).shake(); // shake smartphone
            }
            else{
                // create a snackbar with a positive message
                Snackbar snackbar = Snackbar.make(getView(), R.string.snackbar_send_positive, Snackbar.LENGTH_LONG);
                snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary))
                        .setAction(getString(R.string.snackbar_close_btn), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                            }
                        });
                snackbar.show();
            }
        }
    };

    private boolean deleteBitmapFile(){
        File file = new File(getContext().getFilesDir() + "/" + mEventTitle + ".jpeg");
        return file.delete();
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
}