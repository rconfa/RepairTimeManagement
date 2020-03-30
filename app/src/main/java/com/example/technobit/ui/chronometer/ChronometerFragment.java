package com.example.technobit.ui.chronometer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.CalendarContract;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;

import com.example.technobit.R;
import com.example.technobit.contactdatas.Singleton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.TimeZone;

// TODO: not send on calendar if color/email not selected

public class ChronometerFragment extends Fragment {

    private ChronometerViewModel chronometerViewModel;
    private Chronometer chronometer;
    private long pauseOffset;
    private boolean running; // mi dice se il cronometro sta runnando
    private FloatingActionButton fab_chrono_play;
    private Button buttonStop;
    private Spinner sp;
    private String EventTitle = "";
    private int spinnerSelectionPos;
    private LinearLayout layButtonStop;
    private TextView tv_play;
    private Animation animBlink;
    private SharedPreferences sharedPref;
    private Singleton sg;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // model della classe
        chronometerViewModel = ViewModelProviders.of(this).get(ChronometerViewModel.class);
        View root = inflater.inflate(R.layout.fragment_chronometer, container, false);

        // richiedo i permessi di accesso al calendar se necessario
        permission_calendar();

        // salvo le shared preference per gestire lettura/salvataggio delle preferenze già inserite
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        // save the singleton instance
        sg = Singleton.getInstance(getContext());

        // prendo tutti gli oggetti ui necessari
        // cronometro
        chronometer = root.findViewById(R.id.chrono);
        // bottone per lo start
        fab_chrono_play = (FloatingActionButton) root.findViewById(R.id.fab_start_pause);
        // bottone per lo stop
        buttonStop = (Button) root.findViewById(R.id.btn_stop);
        buttonStop.setHeight(fab_chrono_play.getHeight());
        buttonStop.setWidth(fab_chrono_play.getWidth());

        // spinner per la lista dei clienti
        sp = (Spinner) root.findViewById(R.id.spinner_choose_client);

        // layer da far sparire con bottone stop e scritta
        layButtonStop = (LinearLayout) root.findViewById(R.id.lay_btn_stop);
        layButtonStop.setVisibility(View.GONE);

        // scritta sotto il bottone play
        tv_play = (TextView) root.findViewById(R.id.txtView_Play);
        // animazione per il cronometo
        // load the animation
        animBlink = AnimationUtils.loadAnimation(getContext(), R.anim.blink);


        // Init dello spinner
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(root.getContext(),
                R.layout.spinner_item, sg.getContactNameList());
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sp.setAdapter(arrayAdapter);


        // LISTENER EVENTI
        // evento sulla scelta di un cliente dallo spinner

        sp.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        fab_chrono_play.show();
                        // se ho la view aggiorno i dati
                        // in alcuni casi è null, tipo quando si ruota lo schermo perchè
                        // non è ancora stata inizializzata!
                        if(view != null) {
                            TextView clientName = (TextView) view.findViewById(R.id.spinnerItemTextView);
                            EventTitle = clientName.getText().toString();
                            spinnerSelectionPos = position;
                        }
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        fab_chrono_play.hide();
                    }
                });


        // evento listener sullo start del cronometro
        fab_chrono_play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startChronometer(SystemClock.elapsedRealtime());
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
        if(chronometer.getText().equals("00:00") == false){
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
            fab_chrono_play.setImageResource(android.R.drawable.ic_media_pause);
            // il bottone stop non è piu usabile
            layButtonStop.setVisibility(View.GONE);
            // Cambio la scritta sotto il bottone, metto "pausa"
            tv_play.setText(getResources().getText(R.string.chrono_pause));
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
            layButtonStop.setVisibility(View.VISIBLE);
            // Cambio la scritta sotto il bottone, metto "start"
            tv_play.setText(getResources().getText(R.string.chrono_start));
            // metto in pausa il cronometro e cambio anche l'immagine del fab
            fab_chrono_play.setImageResource(android.R.drawable.ic_media_play);
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
        // scrivo l'evento sul calendario
        saveOnCalendar(startMillis, endMillis);

        pauseOffset = 0;
        chronometer.setBase(SystemClock.elapsedRealtime()); // azzero il cronometro
        // lo spinner deve essere bloccato sul cliente scelto
        sp.setEnabled(false);
        // rimetto il bottone dello stop invisibile, non posso usarlo!
        layButtonStop.setVisibility(View.GONE);
    }

    private void saveOnCalendar(long startMillis, long endMillis) {
        ContentResolver cr = this.getContext().getContentResolver();
        String calID = searchID(cr); // cerco l'id del calendario specifico

        if (!calID.equals("-1")) {
            TimeZone tz = TimeZone.getDefault();

            ContentValues values = new ContentValues();

            int color = getColorInt();


            // mettere controllo id!=-1
            values.put(CalendarContract.Events.DTSTART, startMillis);
            values.put(CalendarContract.Events.DTEND, endMillis);
            values.put(CalendarContract.Events.TITLE, EventTitle);
            values.put(CalendarContract.Events.CALENDAR_ID, calID);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, tz.getID());
            values.put(CalendarContract.Events.EVENT_COLOR_KEY, color);
            // provo ad aggiungere l'evento
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (getContext().checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                        // richiedo il permesso alla scrittura del calendario
                        requestPermissions(new String[]{Manifest.permission.WRITE_CALENDAR}, 0);
                        System.out.println("TODO: SAVE EVENT");
                        shakeIt();
                        //saveEventNotAdd(startMillis,endMillis, Title);
                    }
                    else {
                        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
                        Toast.makeText(this.getContext(), "INVIATO!", Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
                    Toast.makeText(this.getContext(), "INVIATO!", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                System.out.println("TODO: SAVE EVENT");
                shakeIt();
                //saveEventNotAdd(startMillis,endMillis, Title);
            }
        }
        else {
            System.out.println("TODO: SAVE EVENT");
            shakeIt();
        }
            //saveEventNotAdd(startMillis,endMillis, Title);
    }

    private String searchID(ContentResolver cr){
        // cerco l'id del calendario che mi serve:
        String projection[] = {"_id"}; // richiedo l'id del calendario
        // voglio che il nome del calendario corrisponda a questo
        //"technobit.sas@gmail.com"

        // salvo le shared preference per gestire lettura/salvataggio delle preferenze già inserite
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        // prendo l'indirizzo email dalle shared preference, se non c'è viene settata a null
        String email_selected = sharedPref.getString(getString(R.string.shared_email), null);

        String[] selectionArgs = new String[]{ email_selected };

        // eseguo la query richiedendo che il campo name sia quello specificato in selectionArgs

        Cursor managedCursor = cr.query(Uri.parse("content://com.android.calendar/calendars"),
                projection, "calendar_displayName=?", selectionArgs, null);

        // scorro tutti i possibili calendari
        String calID = "-1";

        try {
            managedCursor.moveToFirst();// provo a prendere il primo elemento

            calID = managedCursor.getString(0); // prendo il suo id

            managedCursor.close();
        }
        catch(NullPointerException ne){
            return "-1";
        }
        catch(Exception e) {
            return "-1";
        }
        return calID;
    }

    private int getColorInt(){
        // salvo le shared preference per gestire lettura/salvataggio delle preferenze già inserite
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        int defaultValue = getResources().getInteger(R.integer.default_color);
        int color_selected = sharedPref.getInt(getString(R.string.shared_saved_color), defaultValue);

        int[] mColorChoices=null;
        String[] color_array = getResources().getStringArray(R.array.default_color_choice_values);

        int colorVal = -1;
        if (color_array!=null && color_array.length>0) {
            int i = 0;
            while(i < color_array.length || colorVal == -1){
                if(Color.parseColor(color_array[i]) == color_selected)
                    colorVal = ++i;
                ++i;
            }
        }

        // se non ho trovato il colore di default metto 1 altrimenti restituisco il colore giusto
        return colorVal == -1 ?  1 : colorVal;
    }

    // se non va a buon fine l'invio attivo la vibrazione del telefono
    private void shakeIt() {
        boolean canVib = sharedPref.getBoolean(getString(R.string.shared_vibration), true);

        if (canVib) {
            if (Build.VERSION.SDK_INT >= 26) {
                ((Vibrator) this.getContext().getSystemService(getContext().VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(600, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                ((Vibrator) this.getContext().getSystemService(getContext().VIBRATOR_SERVICE)).vibrate(600);
            }
        }
    }


}