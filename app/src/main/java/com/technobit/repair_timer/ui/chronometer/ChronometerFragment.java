package com.technobit.repair_timer.ui.chronometer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.snackbar.Snackbar;
import com.technobit.repair_timer.R;
import com.technobit.repair_timer.databinding.FragmentChronometerBinding;
import com.technobit.repair_timer.repositories.dataNotSent.GoogleDataSingleton;
import com.technobit.repair_timer.ui.customize.dialog.ConfirmChoiceDialog;
import com.technobit.repair_timer.utils.Constants;
import com.technobit.repair_timer.utils.SmartphoneControlUtility;
import com.technobit.repair_timer.viewmodels.SharedViewModel;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.util.Calendar;
import java.util.Date;

public class ChronometerFragment extends Fragment{
    private static final String TAG = "ChronometerFragment";
    private long mPauseOffset;
    private boolean chronoIsRunning; // mi dice se il cronometro sta runnando
    private String mEventTitle = "", mEmail = "";
    private int mSpinnerSelectionPos;
    private Animation mAnimBlink;
    private SharedPreferences mSharedPref;
    private NavController mNavigator;
    private FragmentChronometerBinding mBinding;

    public ChronometerFragment() {
        // Required empty public constructor
    }

    public static ChronometerFragment newInstance() {
        return new ChronometerFragment();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentChronometerBinding.inflate(inflater, container, false);
        return mBinding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // save shared preference to manage existing preference
        mSharedPref = requireContext().getSharedPreferences(
                Constants.CHRONOMETER_SHARED_PREF_FILENAME, Context.MODE_PRIVATE);

        // if the user don't select the email I ask if he want to choose it now.
        if (!checkAccountSelected())
            askToSelectEmail();

        // load chronometer animation
        mAnimBlink = AnimationUtils.loadAnimation(getContext(), R.anim.blink);

        final SharedViewModel model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Init dello spinner
        mSpinnerSelectionPos = 0; // Default selected index, 0 = hint for the spinner

        // Updating UI, add all item to the adapter
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(view.getContext(),
                R.layout.spinner_item, model.getContactsName(getContext()));
        mBinding.spinnerChooseClient.setAdapter(arrayAdapter);
        //title for the search box
        mBinding.spinnerChooseClient.setTitle(getString(R.string.spinner_hint));
        mBinding.spinnerChooseClient.setPositiveButton(getString(R.string.dialog_btn_yes)); //unused

        // LISTENER
        // event on client choose from the spinner
        mBinding.spinnerChooseClient.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // If the view is not null (ex: rotate the screen produce null view)
                        if(view != null) {
                            // get the client name
                            TextView clientName = view.findViewById(R.id.spinnerItemTextView);
                            // set the event title as the client name
                            mEventTitle = clientName.getText().toString();
                            mSpinnerSelectionPos = position; // save the spinner position
                            System.err.println("Item sel: " + mSpinnerSelectionPos);
                        }
                    }

                    public void onNothingSelected(AdapterView<?> parent) {}
                });


        // Event on chronometer start
        mBinding.fabStartPause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // check if the user have selected a client
                if(mSpinnerSelectionPos != 0){
                    // Spinner is not enable yet.
                    mBinding.spinnerChooseClient.setEnabled(false);

                    mEmail = model.getContactsEmail(getContext(), mSpinnerSelectionPos);
                    System.err.println("chrono: " + mSpinnerSelectionPos + " email:" + mEmail);
                    startChronometer(SystemClock.elapsedRealtime());
                }
                else{
                    // snackbar to send an Hint to the user
                    Snackbar snackbar = Snackbar.make(requireView(),
                            R.string.snackbar_start_error, Snackbar.LENGTH_LONG);
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

        // event on chronometer stop
        mBinding.fabStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopChronometer();
            }
        });

        mNavigator = NavHostFragment.findNavController(this);
    }


    @Override
    public void onResume(){

        int state = mSharedPref.getInt(Constants.CHRONOMETER_SHARED_PREF_STATE,2);

        // state = 2 nothing to recover
        if(state != 2) {
            int sp_pos = mSharedPref.getInt(Constants.CHRONOMETER_SHARED_PREF_SELECTED_CONTACT,0);
            mBinding.spinnerChooseClient.setSelection(sp_pos); // set the choosen client
            mBinding.spinnerChooseClient.setEnabled(false);

            chronoIsRunning = false;

            // If there is something to recover, 0=running, 1=pausa
            if (state == 0) {
                long base = mSharedPref.getLong(Constants.CHRONOMETER_SHARED_PREF_TIME,
                        SystemClock.elapsedRealtime());
                mPauseOffset = 0;
                startChronometer(base);
            } else if (state == 1) {
                mPauseOffset = mSharedPref.getLong(Constants.CHRONOMETER_SHARED_PREF_TIME_PAUSE,0);
                mBinding.chrono.setBase(SystemClock.elapsedRealtime() - mPauseOffset);
                chronoIsRunning = true;
                pauseChronometer();
            }
        }

        // Delete the used preference
        mSharedPref.edit().clear().apply();

        super.onResume();
    }

    @Override
    public void onDestroyView() {
        // save the state
        saveAll();
        super.onDestroyView();
        mBinding = null;
    }

    @Override
    public void onPause(){
        // If the chronometer is init() save the state
        saveAll();
        super.onPause();
    }


    private void saveAll(){
        mBinding.chrono.stop();
        SharedPreferences.Editor editor = mSharedPref.edit();


        // If chronometer not equal 00:00 then there is an exe to be saved
        if(!mBinding.chrono.getText().equals("00:00")){
            // save the spinner position
            editor.putInt(Constants.CHRONOMETER_SHARED_PREF_SELECTED_CONTACT, mSpinnerSelectionPos);

            // Save the chronometer base
            editor.putLong(Constants.CHRONOMETER_SHARED_PREF_TIME, mBinding.chrono.getBase());

            // Save if the chronometer is running or is on pause
            if(chronoIsRunning){
                editor.putInt(Constants.CHRONOMETER_SHARED_PREF_STATE,0); // 0 = running
            }
            else{ // was in pause
                editor.putInt(Constants.CHRONOMETER_SHARED_PREF_STATE,1); // 1 = pause
                editor.putLong(Constants.CHRONOMETER_SHARED_PREF_TIME_PAUSE, mPauseOffset);
            }

        }
        else
            editor.putInt(Constants.CHRONOMETER_SHARED_PREF_STATE,2); // 2 = stop, not start yet


        // Write all preferences
        editor.apply();
    }

    private void startChronometer(long base) {
        // start the chronometer if is not running yet
        if (!chronoIsRunning) {
            // cancel the cronometer animation
            mBinding.chrono.clearAnimation();
            // change the image for the button play->pause
            mBinding.fabStartPause.setImageResource(R.drawable.ic_pause_70dp);
            // button stop can not be used during running
            mBinding.fabStop.setVisibility(View.GONE);
            // Set the chronometer base minus the paused time
            mBinding.chrono.setBase(base - mPauseOffset);
            mBinding.chrono.start();
            mPauseOffset = 0; // set the pause to zero.
            chronoIsRunning = true;
        }
        else { // Chronometer is running so I set it on pause
            pauseChronometer();
        }

    }

    private void pauseChronometer() {
        if (chronoIsRunning) {
            // start the animation for the chronometer
            mBinding.chrono.startAnimation(mAnimBlink);
            // Set button stop visible
            mBinding.fabStop.setVisibility(View.VISIBLE);
            // change the image for the button pause->play
            mBinding.fabStartPause.setImageResource(R.drawable.ic_play_arrow_black_70dp);
            mBinding.chrono.stop();
            // Save the duration of the pause
            mPauseOffset = SystemClock.elapsedRealtime() - mBinding.chrono.getBase();
            chronoIsRunning = false;
        }
    }

    private void stopChronometer(){
        mBinding.chrono.stop();

        // clear the animation
        mBinding.chrono.clearAnimation();
        // set the chronometer as not running
        chronoIsRunning = false;

        // get the ended date
        Date end =  Calendar.getInstance().getTime();
        // time of duration
        long elapsedMillis = (SystemClock.elapsedRealtime() - mBinding.chrono.getBase());

        // Initialize the instance of CalendarEvent with the known value.
        GoogleDataSingleton.initialize(3, mEventTitle, null,
                null, elapsedMillis, end.getTime(), mEmail);

        // go to the signature fragment to complete the action
        mNavigator.navigate(R.id.nav_signature);

        // reset all the chronometer to initial values
        mPauseOffset = 0;
        mBinding.chrono.setBase(SystemClock.elapsedRealtime());
        mBinding.spinnerChooseClient.setEnabled(true);
        mBinding.fabStop.setVisibility(View.GONE);
    }

    private boolean checkAccountSelected(){
        // check if the user has selected an account
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());

        return account != null;
    }

    private void askToSelectEmail() {
        // get the message from the resource
        String message = getString(R.string.dialog_confirm_account_not_selected_yet);
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new ConfirmChoiceDialog(" ", message,mSelectNewEmailListener);
        dialog.show(getParentFragmentManager(), TAG);


    }

    private ConfirmChoiceDialog.NoticeDialogListener mSelectNewEmailListener =
            new ConfirmChoiceDialog.NoticeDialogListener() {
        @Override
        public void onDialogPositiveClick() {
            // go to the tools fragment to select email
            mNavigator.navigate(R.id.action_nav_chrono_to_tools_rip_account);
        }

        @Override
    public void onDialogCancel() {

    }
};

}