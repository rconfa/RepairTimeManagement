package com.example.technobit.ui.tools;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.example.technobit.Color.ColorPickerDialog;
import com.example.technobit.Color.ColorPickerSwatch;
import com.example.technobit.R;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import static android.app.Activity.RESULT_OK;

public class ToolsFragment extends PreferenceFragmentCompat {

    private final static int PICK_ACCOUNT_REQUEST = 0;
    private SharedPreferences sharedPref;
    private String email_selected; // Email selezionata
    private Preference account_sel; // preference sulla scelta dell'account
    private Preference color_sel; // preference sulla scelta del colore
    private int color_selected; // colore selezionato
    private SwitchPreferenceCompat vibration;


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        //toolsViewModel = ViewModelProviders.of(this).get(ToolsViewModel.class);
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // salvo le shared preference per gestire lettura/salvataggio delle preferenze già inserite
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        // preference per l'account del fragment (XML)
        account_sel = (Preference)findPreference("google_account");
        // preference per il colore degli eventi del fragment (XML)
        color_sel = (Preference)findPreference("google_color");
        // preference per la vibrazione del fragment (XML)
        vibration = (SwitchPreferenceCompat)findPreference("notifications");



        // prendo il colore dalle shared preference, se non c'è viene settata al colore di default
        int defaultValue = getResources().getInteger(R.integer.default_color);
        color_selected = sharedPref.getInt(getString(R.string.shared_saved_color), defaultValue);
        // setto l'icona del colore
        set_icon_color();


        // prendo l'indirizzo email dalle shared preference, se non c'è viene settata a null
        email_selected = sharedPref.getString(getString(R.string.shared_email), null);
        // aggiorno il summary dell'account
        set_summary_account();

        // event su "seleziona un account"
        account_sel.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                account_choose(); // metodo che crea l'intent per scegliere l'account
                // aggiorno il summary
                account_sel.setSummary(email_selected);
                return false;
            }
        });

        // event su "seleziona un colore"
        color_sel.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                color_choose(); // metodo che crea l'intent per scegliere il colore degli eventi
                return false;
            }
        });


        vibration.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                //vibration.isChecked()
                // salvo la scelta sulla vibrazione come coppia chiave-valore
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.shared_vibration), vibration.isChecked());
                editor.apply();
                return true;
            }
        });
    }

    private void account_choose(){
        Account acc_selected = null;

        // se ho trovato una mail salvata creo l'account collegato a quella email
        if(email_selected != null)
            acc_selected = new Account(email_selected, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);

        String select_account = getResources().getString(R.string.tools_select_account) +":";
        Intent intent = AccountPicker.newChooseAccountIntent(acc_selected, null,
                new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE},
                false, select_account, null, null, null);
        /*
        .putExtra("overrideTheme", 1)
        .putExtra("overrideCustomTheme",0);
        */

        startActivityForResult(intent, PICK_ACCOUNT_REQUEST);
    }


    private void color_choose(){
        // prendo tutta la lista di colori definita in resource
        int[] mColor = colorChoice();
        int defaultValue = getResources().getInteger(R.integer.default_color);
        color_selected = sharedPref.getInt(getString(R.string.shared_saved_color), defaultValue);

        ColorPickerDialog colorcalendar = ColorPickerDialog.newInstance(
                R.string.color_picker_default_title, mColor,
                color_selected, 5,
                isTablet() ? ColorPickerDialog.SIZE_LARGE
                        : ColorPickerDialog.SIZE_SMALL);


        colorcalendar.setOnColorSelectedListener(colorcalendarListener);

        colorcalendar.show(this.getFragmentManager(), "cal");

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_ACCOUNT_REQUEST && resultCode == RESULT_OK) {
            email_selected = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            set_summary_account();
            // salvo l'email scelta come coppia chiave-valore
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.shared_email), email_selected);
            editor.apply();
        }
    }


    private void set_summary_account(){
        account_sel.setSummary(email_selected); //metto come sommario la mail già selezionata
    }

    private void set_icon_color(){
        // prendo l'immagine del pallino bianco
        Drawable d = getResources().getDrawable(R.drawable.cerchio);

        // cambio il colore
        PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(color_selected, PorterDuff.Mode.SRC_ATOP);

        // setto il filtro colore
        d.setColorFilter(colorFilter);
        color_sel.setIcon(d); // aggiungo l'icona

    }


    // ritorna un vettore di interi di colori, in base a quelli definiti in ./values/string
    private int[] colorChoice(){

        int[] mColorChoices=null;
        String[] color_array = getResources().getStringArray(R.array.default_color_choice_values);

        if (color_array!=null && color_array.length>0) {
            mColorChoices = new int[color_array.length];
            for (int i = 0; i < color_array.length; i++) {

                mColorChoices[i] = Color.parseColor(color_array[i]);
            }
        }
        return mColorChoices;
    }

    // metodo che restituisce true se sto runnando su un tablet
    private boolean isTablet() {
        return (getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    // evento sulla scelta del colore
    private ColorPickerSwatch.OnColorSelectedListener colorcalendarListener = new ColorPickerSwatch.OnColorSelectedListener(){

        @Override
        public void onColorSelected(int color) {
            color_selected = color;

            set_icon_color();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(getString(R.string.shared_saved_color), color_selected);
            editor.apply();
        }
    };
}