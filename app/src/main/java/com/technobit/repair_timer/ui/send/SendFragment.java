package com.technobit.repair_timer.ui.send;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.technobit.repair_timer.R;
import com.technobit.repair_timer.databinding.FragmentSendBinding;
import com.technobit.repair_timer.repositories.dataNotSent.GoogleData;
import com.technobit.repair_timer.service.google.GoogleAsyncResponse;
import com.technobit.repair_timer.service.google.calendar.InsertToGoogleCalendar;
import com.technobit.repair_timer.service.google.drive.InsertToGoogleDrive;
import com.technobit.repair_timer.ui.customize.dialog.colorDialog.ColorUtility;
import com.technobit.repair_timer.utils.Constants;
import com.technobit.repair_timer.utils.SmartphoneControlUtility;
import com.technobit.repair_timer.viewmodels.SendViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class SendFragment extends Fragment  {
    private int parsedData, totalData, mEventColor;
    private Context mContext;
    private FragmentSendBinding mBinding;
    private SharedPreferences mSharedPref;
    private SendViewModel mSendViewModel;
    public SendFragment() {
        // Required empty public constructor
    }

    public static SendFragment newInstance() {
        return new SendFragment();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mBinding = FragmentSendBinding.inflate(inflater, container, false);
        parsedData = 0;
        totalData = 0;
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSharedPref = requireContext().getSharedPreferences(
                Constants.SEND_SHARED_PREF_FILENAME, Context.MODE_PRIVATE);

        boolean canSend = mSharedPref.getBoolean(Constants.SEND_SHARED_PREF_ALLOW, true);

        if(canSend) {
            // get the context
            mContext = getContext();

            // Create a ViewModel the first time the system calls a Fragment's onViewCreated() method.
            // Re-created fragments receive the same mContactViewModel instance created by the first Fragment.
            mSendViewModel = new ViewModelProvider(requireActivity()).get(SendViewModel.class);

            // The observer associated with the object that holds the list of data to send.
            final Observer<ArrayList<GoogleData>> observer = new Observer<ArrayList<GoogleData>>() {
                @Override
                public void onChanged(ArrayList<GoogleData> datas) {
                    if(datas.size() > 0){
                        // set the button for sending all visible
                        mBinding.btnSend.setVisibility(View.VISIBLE);
                        setButtonListener();
                        // setting text
                        mBinding.textInfo.setText(getString(R.string.send_text_info, datas.size()));
                        mBinding.progressBar.setMax(datas.size());
                    }
                }

            };

            // The LiveData object that holds the list of data to send.
            LiveData<ArrayList<GoogleData>> liveData = mSendViewModel.getDatas(mContext);

            /*
             * We set the relationship between the Observer and the LiveData object
             * that holds the Resource associated with the list of contacts.
             */
            liveData.observe(getViewLifecycleOwner(), observer);


            // The observer associated with the object that holds the list of data to send.
            final Observer<Integer> progressObserver = new Observer<Integer>() {
                @Override
                public void onChanged(Integer value) {
                    // progress value update
                    mBinding.progressBar.setProgress(value);
                    mBinding.textPercentage.setText(
                            getString(R.string.send_text_progress, value));
                }

            };

            // The LiveData object that holds the % of progress.
            LiveData<Integer> progress = mSendViewModel.getProgress();

            /*
             * We set the relationship between the Observer and the LiveData object
             * that holds the % of progress.
             */
            progress.observe(getViewLifecycleOwner(), progressObserver);
        }
        else
            mBinding.textInfo.setText(getString(R.string.background_send_feedback));


    }

    private void setButtonListener(){
        mBinding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(new SmartphoneControlUtility(mContext).checkInternetConnection()) {
                    SharedPreferences.Editor editor = mSharedPref.edit();
                    editor.putBoolean(Constants.SEND_SHARED_PREF_ALLOW, false);
                    editor.apply();

                    mBinding.btnSend.setVisibility(View.GONE);
                    mBinding.textInfo.setText(getString(R.string.send_text_upload));
                    mBinding.linearLayoutProgress.setVisibility(View.VISIBLE);
                    SendAllToGoogle();
                }
                else {
                    // snackbar to send an Hint to the user
                    Snackbar snackbar = Snackbar.make(requireView(),
                            R.string.snackbar_no_internet, Snackbar.LENGTH_LONG);
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
        });
    }

    private void SendAllToGoogle() {
        GoogleData singleData;
        totalData = mSendViewModel.getDataSize();
        mEventColor = getColorInt(); // get the color that the user has choose
        for (int parsingIndex = 0; parsingIndex < totalData; parsingIndex++){
            singleData = mSendViewModel.getSingleData(parsingIndex);

            if (singleData != null && singleData.getCase() != 2) {
                sendToCalendar(singleData, parsingIndex);
            }
            else if (singleData != null){
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

                mSendViewModel.clearNullData();
                if(mSendViewModel.getDataSize()>0){
                    mBinding.textInfo.setText(getString(R.string.send_text_fail));
                }
                else
                    mBinding.textInfo.setText(getString(R.string.send_text_completed));
                // update all values into repository
                mSendViewModel.updateDatas(mContext);
                SharedPreferences.Editor editor = mSharedPref.edit();
                editor.clear(); // remove all values
                editor.apply();
            }
        }.start();

    }


    private void sentToDrive(final GoogleData singleData, final int index) {
        final File imageFile = getBitmapFile(singleData.getImage());
        new InsertToGoogleDrive(imageFile, mContext,
                new GoogleAsyncResponse(){
                    @Override
                    public void processFinish(String attachment) {
                        if (!attachment.equals("false")) {
                            singleData.setImage(attachment);
                            // update data in the arrayList
                            mSendViewModel.updateSingleData(index, singleData);
                            // delete the image
                            imageFile.delete();

                            sendToCalendar(singleData, index);
                        }
                        else{
                            parsedData++;
                            int val = (parsedData * 100) / totalData; // calculate the progress
                            safe_update_ui(val); // Update ui
                            safe_vibrate();
                        }
                    }
                }).start();
    }

    private void sendToCalendar(final GoogleData data, final int index){
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
                            mSendViewModel.updateSingleData(index, null);
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
                    /*
                    Log.d("SEND", Integer.toString(totalData));
                    Log.d("SEND", Integer.toString(parsedData));
                    Log.d("SEND", Integer.toString(value));
                    */

                    mSendViewModel.updateProgress(value);
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