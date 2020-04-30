package com.example.technobit.ui.send;

import android.content.Context;
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
import com.example.technobit.utilities.googleService.GoogleAsyncResponse;
import com.example.technobit.utilities.googleService.calendar.InsertToGoogleCalendar;
import com.example.technobit.utilities.googleService.drive.InsertToGoogleDrive;
import com.example.technobit.utilities.notSendedData.GoogleData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class SendFragment extends Fragment  {

    private SendViewModel sendViewModel;
    private SharedPreferences mSharedPref;
    private ArrayList<GoogleData> allData;
    private int parsedData = 0, totalData = 0;
    private Context mContext;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //sendViewModel = ViewModelProviders.of(this).get(SendViewModel.class);
        View root = inflater.inflate(R.layout.fragment_send, container, false);

        mContext = getContext();
        final TextView textView = root.findViewById(R.id.text_send);

        // Shared preference for get/set all the preference
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);


        try {
            allData = new GoogleData().getAll(mContext);
        } catch (IOException e) {
            allData = null;
        }

        if(allData!=null) {
            textView.setText("find " + allData.size() + " event not sended yet");
            SendAllToGoogle(allData);
        }
        else
            textView.setText("Error on file reading");


        return root;
    }

    private void SendAllToGoogle(final ArrayList<GoogleData> allData) {
        GoogleData singleData;
        totalData = allData.size();
        for (int parsingIndex = 0; parsingIndex < allData.size(); parsingIndex++){
            singleData = allData.get(parsingIndex);
            if (singleData.getCase() != 2) {
                sendToCalendar(singleData);
            }
            else{
                sentToDrive(singleData, parsingIndex);
            }
        }

        new Thread() {
            public void run() {
                while(totalData != parsedData){
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    new GoogleData().writeAll(allData, mContext);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }


    private void sentToDrive(final GoogleData singleData, final int index) {
        final File imageFile = getBitmapFile(singleData.getImage());
        new InsertToGoogleDrive(singleData.getEventTitle(), imageFile, mContext,
                new GoogleAsyncResponse(){
                    @Override
                    public void processFinish(String attachment) {
                        if (!attachment.equals("false")) {
                            singleData.setImage(attachment);
                            // update data in the arrayList
                            allData.set(index, singleData);
                            // delete the image
                            imageFile.delete();

                            sendToCalendar(singleData);
                        }
                    }
                }).start();
    }

    private void sendToCalendar(final GoogleData data){
        // when the image is upload I add the event on calendar
        int color = 3; //getColorInt(); // get the color that the user has choose
        // insert the event on calendar
        Date endDate = new Date(data.getEventEnd());

        new InsertToGoogleCalendar(data.getEventTitle(), data.getDescription(), endDate,
                data.getEventDuration(), color, mContext,data.getImage(),
                new  GoogleAsyncResponse(){
                    @Override
                    public void processFinish(String result) {
                        parsedData++;
                        if(result.equals("true"))
                            allData.remove(data);
                        else
                            System.err.println("to vibbb");
                    }
                }).start();

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

    private File getBitmapFile(String filepath){
        return new File(filepath);
    }

}