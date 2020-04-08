package com.example.technobit.ui.send;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.example.technobit.R;
import com.example.technobit.utilities.notSendedData.GoogleData;

import java.io.IOException;
import java.util.ArrayList;

public class SendFragment extends Fragment {

    private SendViewModel sendViewModel;
    private SharedPreferences mSharedPref;
    private ArrayList<GoogleData> allData;
    private int parsingIndex;
    private int numberOfExecution, totToExecute;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //sendViewModel = ViewModelProviders.of(this).get(SendViewModel.class);
        View root = inflater.inflate(R.layout.fragment_send, container, false);
        final TextView textView = root.findViewById(R.id.text_send);

        // Shared preference for get/set all the preference
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());


        try {
            allData = new GoogleData().getAll(getContext());
        } catch (IOException e) {
            allData = null;
        }

        if(allData!=null) {
            textView.setText("find " + allData.size() + " event not sended yet");
            //SendAllToGoogle(allData);
        }
        else
            textView.setText("Error on file reading");


        // View root = inflater.inflate(R.layout.fragment_signature, container, false);
        return root;
    }

    /*
    private void SendAllToGoogle(ArrayList<GoogleData> allData) {
        GoogleData singleData = new GoogleData();
        numberOfExecution = 0;
        totToExecute = allData.size();

        for (parsingIndex = 0; parsingIndex < allData.size(); parsingIndex++){
            singleData = allData.get(parsingIndex);
            if (singleData.getCase() != 2) {
                sendToCalendar(singleData);
            }
            else{
                sentToDrive(singleData);
            }
        }

        // delete the file
        try {
            new FileGoogle().delete(getContext());
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(GoogleData data:allData) {
            try {
                new FileGoogle().writeAllToFile(data, getContext());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void sentToDrive(GoogleData singleData) {
        // Insert the image on google drive
        AsyncInsertGoogleDrive gDrive = new AsyncInsertGoogleDrive(singleData.getEventTitle(),
                getBitmapFile(singleData.getImage()), getContext(), mDriveResponse);
        gDrive.execute();
    }

    private void sendToCalendar(GoogleData data){
        // when the image is upload I add the event on calendar
        int color = getColorInt(); // get the color that the user has choose
        // insert the event on calendar
        Date endDate = new Date(data.getEventEnd());

        AsyncInsertGoogleCalendar gCal = new AsyncInsertGoogleCalendar(data.getEventTitle(),
                data.getDescription(), endDate, data.getEventDuration(), color, getContext(),
                mCalendarResponse, data.getImage());
        gCal.execute();

    }


    // Implement the interface to handle the asyncTask response for google calendar insert
    private GoogleAsyncResponse mCalendarResponse = new GoogleAsyncResponse(){
        @Override
        public void processFinish(String result) {
            if(result.equals("true"))
                allData.remove(parsingIndex);

            numberOfExecution++;
        }
    };

    // Implement the interface to handle the asyncTask response for google drive uploading
    private GoogleAsyncResponse mDriveResponse = new GoogleAsyncResponse(){
        @Override
        public void processFinish(String attachment) {
            if(attachment != null) {
                GoogleData dataToAdd = allData.get(parsingIndex);

                // delete the bitmap file, is useless now
                getBitmapFile(dataToAdd.getImage()).delete();

                dataToAdd.setImage(attachment);
                // modify the array list in case the google calendar fail
                allData.set(parsingIndex, dataToAdd);
                sendToCalendar(dataToAdd);
            }
        }
    };

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

    private File getBitmapFile(String filepath){
        return new File(filepath);
    }*/
}