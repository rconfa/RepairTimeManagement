package com.example.technobit.ui.chronometer;

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
import android.widget.Chronometer;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.example.technobit.R;
import com.example.technobit.ui.customize.dialog.ConfirmChoiceDialog;
import com.example.technobit.utilities.data.ContactSingleton;
import com.example.technobit.utilities.notSendedData.GoogleDataSingleton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.Date;

// todo: non cancellare clienti durante running
public class ChronometerFragment extends Fragment{
    private static final String TAG = "ChronometerFragment";
    private ChronometerViewModel chronometerViewModel;
    private Chronometer mChronometer;
    private long mPauseOffset;
    private boolean mChronometerIsRunnig; // mi dice se il cronometro sta runnando
    private FloatingActionButton mFabChronoPlay, mFabStop;
    private Spinner mSpinnerContact;
    private String mEventTitle = "";
    private int mSpinnerSelectionPos;
    private Animation mAnimBlink;
    private SharedPreferences mSharedPref;
    private NavController mNavigator;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // model della classe
        chronometerViewModel = ViewModelProviders.of(this).get(ChronometerViewModel.class);
        View root = inflater.inflate(R.layout.fragment_chronometer, container, false);

        // salvo le shared preference per gestire lettura/salvataggio delle preferenze già inserite
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        // if the user don't select the email I ask if he want to choose it now.
        if (!checkAccountSelected())
            askToSelectEmail();

        // get the singleton instance
        ContactSingleton sg = ContactSingleton.getInstance(getContext());

        // Get all ui object
        mChronometer = root.findViewById(R.id.chrono);
        // fab button for start/pause chronometer
        mFabChronoPlay = root.findViewById(R.id.fab_start_pause);
        // Button for stop chronometer
        mFabStop = root.findViewById(R.id.fab_stop);

        // spinner for client list
        mSpinnerContact = root.findViewById(R.id.spinner_choose_client);

        // load chronometer animation
        mAnimBlink = AnimationUtils.loadAnimation(getContext(), R.anim.blink);


        // Init dello spinner
        mSpinnerSelectionPos = 0; // Default selected index, 0 = hint for the spinner
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(root.getContext(),
                R.layout.spinner_item, sg.getContactNameList());
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        arrayAdapter.insert(getResources().getString(R.string.spinner_hint),0);
        mSpinnerContact.setAdapter(arrayAdapter);


        // LISTENER
        // event on client choose from the spinner
        mSpinnerContact.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // If the view is not null (ex: rotate the screen produce null view)
                        if(view != null) {
                            // get the client name
                            TextView clientName = view.findViewById(R.id.spinnerItemTextView);
                            // set the event title as the client name
                            mEventTitle = clientName.getText().toString();
                            mSpinnerSelectionPos = position; // save the spinner position
                        }
                    }

                    public void onNothingSelected(AdapterView<?> parent) {}
                });


        // Event on chronometer start
        mFabChronoPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // check if the user have selected a client
                if(mSpinnerSelectionPos != 0){
                    // Spinner is not enable yet.
                    mSpinnerContact.setEnabled(false);
                    startChronometer(SystemClock.elapsedRealtime());
                }
                else{
                    // snackbar to send an Hint to the user
                    Snackbar snackbar = Snackbar.make(getView(), R.string.snackbar_start_error, Snackbar.LENGTH_LONG);
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
        mFabStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopChronometer();
            }
        });

        mNavigator = NavHostFragment.findNavController(this);

        return root;
    }


    @Override
    public void onResume(){

        int state = mSharedPref.getInt("chrono_state", 2);

        // state = 2 nothing to recover
        if(state != 2) {
            int sp_pos = mSharedPref.getInt("spinner_pos", 0);
            mSpinnerContact.setSelection(sp_pos); // set the choosen client
            mSpinnerContact.setEnabled(false);

            mChronometerIsRunnig = false;

            // If there is something to recover, 0=running, 1=pausa
            if (state == 0) {
                long base = mSharedPref.getLong("chronoBase", SystemClock.elapsedRealtime());
                mPauseOffset = 0;
                startChronometer(base);
            } else if (state == 1) {
                mPauseOffset = mSharedPref.getLong("timeAppStopped", 0);
                mChronometer.setBase(SystemClock.elapsedRealtime() - mPauseOffset);
                mChronometerIsRunnig = true;
                pauseChronometer();
            }
        }

        // Delete the used preference
        mSharedPref.edit().remove("chrono_state").remove("spinner_pos")
                .remove("chronoBase").remove("timeAppStopped").apply();


        super.onResume();
    }

    @Override
    public void onDestroy() {
        // If the chronometer is init() save the state
        if(mChronometer != null)
            saveAll();

        super.onDestroy();
    }

    @Override
    public void onPause(){
        // If the chronometer is init() save the state
        if(mChronometer != null)
            saveAll();
        super.onPause();
    }

    private void saveAll(){
        mChronometer.stop();
        SharedPreferences.Editor editor = mSharedPref.edit();


        // If chronometer not equal 00:00 then there is an exe to be saved
        if(!mChronometer.getText().equals("00:00")){
            // save the spinner position
            editor.putInt("spinner_pos", mSpinnerSelectionPos);

            // Save the chronometer base
            editor.putLong("chronoBase", mChronometer.getBase());

            // Save if the chronometer is running or is on pause
            if(mChronometerIsRunnig){
                editor.putInt("chrono_state", 0); // 0 = running
            }
            else{ // was in pause
                editor.putInt("chrono_state", 1); // 1 = pause
                editor.putLong("timeAppStopped", mPauseOffset);
            }

        }
        else
            editor.putInt("chrono_state", 2); // 2 = stop, not start yet


        // Write in synchronized mode all the preference
        editor.commit();
    }

    private void startChronometer(long base) {
        // start the chronometer if is not running yet
        if (!mChronometerIsRunnig) {
            // cancel the cronometer animation
            mChronometer.clearAnimation();
            // change the image for the button play->pause
            mFabChronoPlay.setImageResource(R.drawable.ic_pause_70dp);
            // button stop can not be used during running
            mFabStop.setVisibility(View.GONE);
            // Set the chronometer base minus the paused time
            mChronometer.setBase(base - mPauseOffset);
            mChronometer.start();
            mPauseOffset = 0; // set the pause to zero.
            mChronometerIsRunnig = true;
        }
        else { // Chronometer is running so I set it on pause
            pauseChronometer();
        }

    }

    private void pauseChronometer() {
        if (mChronometerIsRunnig) {
            // start the animation for the chronometer
            mChronometer.startAnimation(mAnimBlink);
            // Set button stop visible
            mFabStop.setVisibility(View.VISIBLE);
            // change the image for the button pause->play
            mFabChronoPlay.setImageResource(R.drawable.ic_play_arrow_black_70dp);
            mChronometer.stop();
            // Save the duration of the pause
            mPauseOffset = SystemClock.elapsedRealtime() - mChronometer.getBase();
            mChronometerIsRunnig = false;
        }
    }

    private void stopChronometer(){
        mChronometer.stop();

        // clear the animation
        mChronometer.clearAnimation();
        // set the chronometer as not running
        mChronometerIsRunnig = false;

        // get the ended date
        Date end =  Calendar.getInstance().getTime();
        // time of duration
        long elapsedMillis = (SystemClock.elapsedRealtime() - mChronometer.getBase());

        // Initialize the instance of CalendarEvent with the known value.
        GoogleDataSingleton.initialize(3, mEventTitle, null,
                null, elapsedMillis, end.getTime());

        // go to the signature fragment to complete the action
        mNavigator.navigate(R.id.nav_signature);

        // reset all the chronometer to initial values
        mPauseOffset = 0;
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mSpinnerContact.setEnabled(true);
        mFabStop.setVisibility(View.GONE);
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
    };

}