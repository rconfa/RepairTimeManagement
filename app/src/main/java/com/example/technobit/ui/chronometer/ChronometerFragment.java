package com.example.technobit.ui.chronometer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;

import com.example.technobit.R;
import com.example.technobit.contactdatas.Singleton;
import com.example.technobit.ui.customize.ConfirmChoiceDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;


/* TODO: Better dialog message for email checking
*        send to calendar(check if email selected)
* */

public class ChronometerFragment extends Fragment implements ConfirmChoiceDialog.NoticeDialogListener{
    private static final String TAG = "ChronometerFragment";
    private ChronometerViewModel chronometerViewModel;
    private Chronometer chronometer;
    private long pauseOffset;
    private boolean running; // mi dice se il cronometro sta runnando
    private FloatingActionButton fabChronoPlay;
    private Spinner sp;
    private String EventTitle = "";
    private int spinnerSelectionPos;
    private LinearLayout linearLayButtonStop;
    private TextView tvPlay;
    private Animation animBlink;
    private SharedPreferences sharedPref;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // model della classe
        chronometerViewModel = ViewModelProviders.of(this).get(ChronometerViewModel.class);
        View root = inflater.inflate(R.layout.fragment_chronometer, container, false);

        // salvo le shared preference per gestire lettura/salvataggio delle preferenze già inserite
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        // if the user don't select the email I ask if he want to choose it now.
        if (!checkAccountSelected())
            askToSelectEmail();


        // save the singleton instance
        Singleton sg = Singleton.getInstance(getContext());

        // Get all ui object
        chronometer = root.findViewById(R.id.chrono);
        // fab button for start/pause chronometer
        fabChronoPlay = root.findViewById(R.id.fab_start_pause);
        // Button for stop chronometer
        Button buttonStop = root.findViewById(R.id.btn_stop);
        // set size like the fab button
        buttonStop.setHeight(fabChronoPlay.getHeight());
        buttonStop.setWidth(fabChronoPlay.getWidth());

        // spinner for client list
        sp = root.findViewById(R.id.spinner_choose_client);

        // layer to display when chronometer is in pause
        linearLayButtonStop = root.findViewById(R.id.lay_btn_stop);
        linearLayButtonStop.setVisibility(View.GONE);

        // textview under the fab button for play/pause
        tvPlay = root.findViewById(R.id.txtView_Play);
        // load chronometer animation
        animBlink = AnimationUtils.loadAnimation(getContext(), R.anim.blink);


        // Init dello spinner
        spinnerSelectionPos = 0; // Default selected index, 0 = hint for the spinner
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(root.getContext(),
                R.layout.spinner_item, sg.getContactNameList());
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        arrayAdapter.insert(getResources().getString(R.string.spinner_hint),0);
        sp.setAdapter(arrayAdapter);


        // LISTENER
        // event on client choose from the spinner
        sp.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // If the view is not null (ex: rotate the screen produce null view)
                        if(view != null) {
                            // get the client name
                            TextView clientName = view.findViewById(R.id.spinnerItemTextView);
                            // set the event title as the client name
                            EventTitle = clientName.getText().toString();
                            spinnerSelectionPos = position; // save the spinner position
                        }
                    }

                    public void onNothingSelected(AdapterView<?> parent) {}
                });


        // Event on chronometer start
        fabChronoPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // check if the user have selected a client
                if(spinnerSelectionPos != 0){
                    // Spinner is not enable yet.
                    sp.setEnabled(false);
                    startChronometer(SystemClock.elapsedRealtime());
                }
                else{
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
            }
        });


        // event on chronometer stop
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopChronometer();
            }
        });

        return root;
    }


    @Override
    public void onResume(){

        int state = sharedPref.getInt("chrono_state", 2);

        // state = 2 nothing to recover
        if(state != 2) {
            int sp_pos = sharedPref.getInt("spinner_pos", 0);
            sp.setSelection(sp_pos); // set the choosen client
            sp.setEnabled(false);

            running = false;

            // If there is something to recover, 0=running, 1=pausa
            if (state == 0) {
                long base = sharedPref.getLong("chronoBase", SystemClock.elapsedRealtime());
                pauseOffset = 0;
                startChronometer(base);
            } else if (state == 1) {
                pauseOffset = sharedPref.getLong("timeAppStopped", 0);
                chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
                running = true;
                pauseChronometer();
            }
        }

        // Delete the used preference
        sharedPref.edit().remove("chrono_state").remove("spinner_pos")
                .remove("chronoBase").remove("timeAppStopped").apply();


        super.onResume();
    }

    @Override
    public void onDestroy() {
        // If the chronometer is init() save the state
        if(chronometer != null)
            saveAll();

        super.onDestroy();
    }

    @Override
    public void onPause(){
        // If the chronometer is init() save the state
        if(chronometer != null)
            saveAll();
        super.onPause();
    }

    private void saveAll(){
        chronometer.stop();
        SharedPreferences.Editor editor = sharedPref.edit();


        // If chronometer not equal 00:00 then there is an exe to be saved
        if(!chronometer.getText().equals("00:00")){
            // save the spinner position
            editor.putInt("spinner_pos", spinnerSelectionPos);

            // Save the chronometer base
            editor.putLong("chronoBase", chronometer.getBase());

            // Save if the chronometer is running or is on pause
            if(running){
                editor.putInt("chrono_state", 0); // 0 = running
            }
            else{ // was in pause
                editor.putInt("chrono_state", 1); // 1 = pause
                editor.putLong("timeAppStopped", pauseOffset);
            }

        }
        else
            editor.putInt("chrono_state", 2); // 2 = stop, not start yet


        // Write in synchronized mode all the preference
        editor.commit();
    }

    private void startChronometer(long base) {
        // start the chronometer if is not running yet
        if (!running) {
            // cancel the cronometer animation
            chronometer.clearAnimation();
            // change the image for the button play->pause
            fabChronoPlay.setImageResource(android.R.drawable.ic_media_pause);
            // button stop can not be used during running
            linearLayButtonStop.setVisibility(View.GONE);
            // Change the text under the button play->pause
            tvPlay.setText(getResources().getText(R.string.chrono_pause));
            // Set the chronometer base minus the paused time
            chronometer.setBase(base  - pauseOffset);
            chronometer.start();
            pauseOffset = 0; // set the pause to zero.
            running = true;
        }
        else { // Chronometer is running so I set it on pause
            pauseChronometer();
        }

    }

    private void pauseChronometer() {
        if (running) {
            // start the animation for the chronometer
            chronometer.startAnimation(animBlink);
            // Set button stop visible
            linearLayButtonStop.setVisibility(View.VISIBLE);
            // Change the text under the button pause->play
            tvPlay.setText(getResources().getText(R.string.chrono_start));
            // change the image for the button pause->play
            fabChronoPlay.setImageResource(android.R.drawable.ic_media_play);
            chronometer.stop();
            // Save the duration of the pause
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            running = false;
        }
    }

    private void stopChronometer(){
        chronometer.stop();

        // clear the animation
        chronometer.clearAnimation();
        // set the chronometer as not running
        running = false;

        long endMillis = System.currentTimeMillis(); // time of end
        // time of duration
        long elapsedMillis = (SystemClock.elapsedRealtime() - chronometer.getBase());
        // time of start
        long startMillis = endMillis - elapsedMillis;

        // go to the signature fragment to complete the action
        // add bundle value
        Bundle bundle = new Bundle();
        bundle.putLong("startMillis", startMillis);
        bundle.putLong("endMillis", endMillis);
        bundle.putString("EventTitle", EventTitle);
        Navigation.findNavController(getView()).navigate(R.id.nav_signature, bundle);


        // reset all the chronometer to initial values
        pauseOffset = 0;
        chronometer.setBase(SystemClock.elapsedRealtime());
        sp.setEnabled(true);
        linearLayButtonStop.setVisibility(View.GONE);
    }

    private boolean checkAccountSelected(){
        // check if the user has selected an account
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());

        return account != null;
    }

    private void askToSelectEmail() {
        // listner for the dialog
        ConfirmChoiceDialog.NoticeDialogListener listener = this;
        // get the message from the resource
        String message = getString(R.string.dialog_confirm_account_not_selected_yet);
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new ConfirmChoiceDialog(" ", message,listener);
        dialog.show(getParentFragmentManager(), TAG);


    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // go to the tools fragment to select email
        Navigation.findNavController(getView()).navigate(R.id.nav_tools);
    }
}