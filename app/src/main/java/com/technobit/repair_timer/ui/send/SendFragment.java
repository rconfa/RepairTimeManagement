package com.technobit.repair_timer.ui.send;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.technobit.repair_timer.R;
import com.technobit.repair_timer.databinding.FragmentSendBinding;
import com.technobit.repair_timer.ui.customize.dialog.colorDialog.ColorUtility;
import com.technobit.repair_timer.utils.Constants;
import com.technobit.repair_timer.utils.SmartphoneControlUtility;
import com.technobit.repair_timer.utils.dataNotSent.GoogleData;
import com.technobit.repair_timer.utils.googleService.GoogleAsyncResponse;
import com.technobit.repair_timer.utils.googleService.calendar.InsertToGoogleCalendar;
import com.technobit.repair_timer.utils.googleService.drive.InsertToGoogleDrive;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

// todo 2: sistemare swap activity mentre thread stanno andando
public class SendFragment extends Fragment  {
    private ArrayList<GoogleData> allData;
    private int parsedData = 0, totalData = 0, mEventColor;
    private Context mContext;
    private FragmentSendBinding mBinding;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mBinding = FragmentSendBinding.inflate(inflater, container,false);
        View view = mBinding.getRoot();

        // get the context
        mContext = getContext();

        try {
            allData = new GoogleData().getAll(mContext);
            if(allData.size() > 0) {
                // set the button for sending all visible
                mBinding.btnSend.setVisibility(View.VISIBLE);
                // setting text
                mBinding.textInfo.setText(getString(R.string.send_text_info, allData.size()));
            }
            else
                mBinding.textInfo.setText(getString(R.string.send_text_error));
        } catch (IOException e) {
            allData = null;
            mBinding.textInfo.setText(getString(R.string.send_text_error));
        }

        mBinding.btnSend.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mBinding.btnSend.setVisibility(View.GONE);
                mBinding.textInfo.setText(getString(R.string.send_text_upload));
                mBinding.linearLayoutProgress.setVisibility(View.VISIBLE);
                mBinding.progressBar.setMax(allData.size());
                mBinding.progressBar.setProgress(0);
                mBinding.textPercentage.setText(getString(R.string.send_text_progress, 0));
                SendAllToGoogle(allData);
            }
        });
        return view;
    }

    private void SendAllToGoogle(final ArrayList<GoogleData> allData) {
        GoogleData singleData;
        totalData = allData.size();
        mEventColor = getColorInt(); // get the color that the user has choose
        for (int parsingIndex = 0; parsingIndex < allData.size(); parsingIndex++){
            singleData = allData.get(parsingIndex);
            if (singleData.getCase() != 2) {
                sendToCalendar(singleData);
            }
            else{
                sentToDrive(singleData, parsingIndex);
            }
        }

        // thread for delete sent data from file when finish.
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
                        else
                            safe_vibrate();
                    }
                }).start();
    }

    private void sendToCalendar(final GoogleData data){
        // when the image is upload I add the event on calendar
        // insert the event on calendar
        Date endDate = new Date(data.getEventEnd());

        new InsertToGoogleCalendar(data.getEventTitle(), data.getDescription(), endDate,
                data.getEventDuration(), mEventColor, mContext,data.getImage(),
                new  GoogleAsyncResponse(){
                    @Override
                    public void processFinish(String result) {
                        parsedData++;
                        int val = (parsedData * 100) / totalData; // calculate the progress
                        safe_update_ui(val); // Update ui

                        if(result.equals("true"))
                            allData.remove(data);
                        else
                            safe_vibrate();
                    }
                }).start();
    }

    private int getColorInt(){
        int defaultColorValue = Color.parseColor(Constants.default_color);
        SharedPreferences sharedPref = requireContext().getSharedPreferences(
                Constants.TOOLS_SHARED_PREF_FILENAME, Context.MODE_PRIVATE);

        // Get the selected color from the shared preference
        int colorSelected = sharedPref.getInt(Constants.TOOLS_SHARED_PREF_GOOGLE_COLOR, defaultColorValue);

        // get all color list defined
        ColorUtility colorUtility = new ColorUtility(getContext());
        ArrayList<Integer> mColor = colorUtility.getColorsArrayList();
        return mColor.indexOf(colorSelected);
    }

    private File getBitmapFile(String filepath){
        return new File(filepath);
    }

    private void safe_update_ui(final int value){
        // shake the smartphone if the fragment is available
        if(this.isAdded()) {
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // progress value update
                    mBinding.progressBar.setProgress(value);
                    mBinding.textPercentage.setText(
                            getString(R.string.send_text_progress, value));
                }
            });
        }
    }

    private void safe_vibrate(){
        // shake the smartphone if the fragment is available
        if(this.isAdded()) {
            // check if the user let vibrate the smartphone
            SharedPreferences mSharedPref = requireContext().getSharedPreferences(
                    Constants.TOOLS_SHARED_PREF_FILENAME, Context.MODE_PRIVATE);
            final boolean canVib = mSharedPref.getBoolean(Constants.TOOLS_SHARED_PREF_VIBRATION, true);

            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (canVib)
                        new SmartphoneControlUtility(getContext()).shake(); // shake smartphone
                }
            });
        }
    }
}