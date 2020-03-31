package com.example.technobit.ui.chronometer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;

import com.example.technobit.R;
import com.example.technobit.contactdatas.Singleton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;


/* TODO: Better dialog message for email checking
*        send to calendar(check if email selected)
* */

public class ChronometerFragment extends Fragment {

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

        // richiedo i permessi di accesso al calendar se necessario
        permission_calendar();

        // salvo le shared preference per gestire lettura/salvataggio delle preferenze già inserite
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        // if the user don't select the email I ask if he want to choose it now.
        if (!checkEmailSelected())
            askToSelectEmail();



        // save the singleton instance
        Singleton sg = Singleton.getInstance(getContext());

        // prendo tutti gli oggetti ui necessari
        // cronometro
        chronometer = root.findViewById(R.id.chrono);
        // bottone per lo start
        fabChronoPlay = root.findViewById(R.id.fab_start_pause);
        // bottone per lo stop
        Button buttonStop = root.findViewById(R.id.btn_stop);
        buttonStop.setHeight(fabChronoPlay.getHeight());
        buttonStop.setWidth(fabChronoPlay.getWidth());

        // spinner per la lista dei clienti
        sp = root.findViewById(R.id.spinner_choose_client);

        // layer da far sparire con bottone stop e scritta
        linearLayButtonStop = root.findViewById(R.id.lay_btn_stop);
        linearLayButtonStop.setVisibility(View.GONE);

        // scritta sotto il bottone play
        tvPlay = root.findViewById(R.id.txtView_Play);
        // animazione per il cronometo
        // load the animation
        animBlink = AnimationUtils.loadAnimation(getContext(), R.anim.blink);


        // Init dello spinner
        spinnerSelectionPos = 0; // Default selected index, 0 = hint for the spinner
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(root.getContext(),
                R.layout.spinner_item, sg.getContactNameList());
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        arrayAdapter.insert(getResources().getString(R.string.spinner_hint),0);
        sp.setAdapter(arrayAdapter);


        // LISTENER EVENTI
        // evento sulla scelta di un cliente dallo spinner

        sp.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        fabChronoPlay.show();
                        // se ho la view aggiorno i dati
                        // in alcuni casi è null, tipo quando si ruota lo schermo perchè
                        // non è ancora stata inizializzata!
                        if(view != null) {
                            TextView clientName = view.findViewById(R.id.spinnerItemTextView);
                            EventTitle = clientName.getText().toString();
                            spinnerSelectionPos = position;
                        }
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        fabChronoPlay.hide();
                    }
                });


        // evento listener sullo start del cronometro
        fabChronoPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(spinnerSelectionPos != 0)
                    startChronometer(SystemClock.elapsedRealtime());
                else{
                    Snackbar snackbar = Snackbar.make(getView(), R.string.snackbar_start_error, Snackbar.LENGTH_LONG);
                    snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary))
                            .setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                }
                            });
                    snackbar.show();
                }
            }
        });


        // evento listener sullo start del cronometro
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

        if(state != 2) {
            int sp_pos = sharedPref.getInt("spinner_pos", 0);
            sp.setSelection(sp_pos);
            sp.setEnabled(false);

            running = false;

            // c'è qualcosa da recuperare, 0=running, 1=pausa
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

        // cancello le preferenze perchè già state utilizzate! Modo di delete = asincrono!
        sharedPref.edit().remove("chrono_state").remove("spinner_pos")
                .remove("chronoBase").remove("timeAppStopped").apply();


        super.onResume();
    }

    @Override
    public void onDestroy() {
        // se il cronometro è stato inizializzato salvo i dati!
        if(chronometer != null)
            saveAll();

        super.onDestroy();
    }

    @Override
    public void onPause(){
        saveAll();
        super.onPause();
    }

    private void saveAll(){
        // se cambia l'orientamento o l'app viene chiusa devo salvare i dati!!!
        chronometer.stop(); // stoppo il cronometro
        SharedPreferences.Editor editor = sharedPref.edit();


        // se non è uguale a 00:00 allora c'è un exe in corso!
        if(!chronometer.getText().equals("00:00")){
            //salvo la posizione dello spinner
            editor.putInt("spinner_pos", spinnerSelectionPos);

            // salvo la base del cronometro
            editor.putLong("chronoBase", chronometer.getBase());

            // il cronometro era attivo, devo continuare a farlo andare!!!
            if(running){
                editor.putInt("chrono_state", 0); // 0 = running
            }
            else{ // era in pausa, lo rimetto in pausa
                editor.putInt("chrono_state", 1); // 1 = pause
                editor.putLong("timeAppStopped", pauseOffset);
            }

        }
        else
            editor.putInt("chrono_state", 2); // 2 = fermo, non partito!


        // scrivo in modo sincrono tutte le shared preferenze
        editor.commit();
    }

    private void permission_calendar(){
        // Richiesta della lettura del calendario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.getContext().checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_CALENDAR}, 0);
            }
        }

        // Richiesta della scrittura del calendario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.getContext().checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_CALENDAR}, 0);
            }
        }
    }

    private void startChronometer(long base) {
        // se il cronometro non sta ancora andando lo faccio partire
        if (!running) {
            // smetto di far lampeggiare il cronometro
            chronometer.clearAnimation();
            // cambio l'immagine con quella della pausa
            fabChronoPlay.setImageResource(android.R.drawable.ic_media_pause);
            // il bottone stop non è piu usabile
            linearLayButtonStop.setVisibility(View.GONE);
            // Cambio la scritta sotto il bottone, metto "pausa"
            tvPlay.setText(getResources().getText(R.string.chrono_pause));
            // lo spinner deve essere bloccato sul cliente scelto
            sp.setEnabled(false);
            // risetto il tempo del cronometro considerando anche la pausa
            chronometer.setBase(base  - pauseOffset);
            chronometer.start();
            pauseOffset = 0; // azzero l'offset della pausa
            running = true;
        }
        else { // sta ancora runnando, se riclicco il bottone lo metto in pausa
            pauseChronometer();
        }

    }

    private void pauseChronometer() {
        if (running) {
            // faccio lampeggiare il cronometro
            chronometer.startAnimation(animBlink);
            // il bottone per lo stop è visibile
            linearLayButtonStop.setVisibility(View.VISIBLE);
            // Cambio la scritta sotto il bottone, metto "start"
            tvPlay.setText(getResources().getText(R.string.chrono_start));
            // metto in pausa il cronometro e cambio anche l'immagine del fab
            fabChronoPlay.setImageResource(android.R.drawable.ic_media_play);
            chronometer.stop();
            // salvo per quanto tempo sono stato in pausa
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            running = false;
        }
    }

    private void stopChronometer(){
        chronometer.stop(); // stoppo il cronometro

        // smetto di far lampeggiare il cronometro
        chronometer.clearAnimation();
        // setto come fermo
        running = false;

        long endMillis = System.currentTimeMillis(); // data e ora in millisecondi di fine
        // conto quanto tempo è stato attivo il chronometro
        long elapsedMillis = (SystemClock.elapsedRealtime() - chronometer.getBase());
        // tempo di fine - tempo attivo = data e ora in millisecondi di inizio
        long startMillis = endMillis - elapsedMillis;

        // go to the signature fragment to complete the action
        // add bundle value
        Bundle bundle = new Bundle();
        bundle.putLong("startMillis", startMillis);
        bundle.putLong("endMillis", endMillis);
        bundle.putString("EventTitle", EventTitle);
        Navigation.findNavController(getView()).navigate(R.id.nav_signature, bundle);


        pauseOffset = 0;
        chronometer.setBase(SystemClock.elapsedRealtime()); // azzero il cronometro
        // lo spinner deve essere bloccato sul cliente scelto
        sp.setEnabled(false);
        // rimetto il bottone dello stop invisibile, non posso usarlo!
        linearLayButtonStop.setVisibility(View.GONE);
    }

    private boolean checkEmailSelected(){
        // prendo l'indirizzo email dalle shared preference, se non c'è viene settata a null
        String email_selected = sharedPref.getString(getString(R.string.shared_email), null);

        return email_selected != null;
    }

    private void askToSelectEmail() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.account_not_selected_yet));
        // Set up the buttons
        builder.setPositiveButton(getString(R.string.dialog_btn_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // go to the tools fragment to select email
                Navigation.findNavController(getView()).navigate(R.id.nav_tools);
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_btn_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();

    }
}