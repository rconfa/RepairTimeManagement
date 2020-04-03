package com.example.technobit.ui.tools;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.example.technobit.R;
import com.example.technobit.ui.colorDialog.ColorPickerDialog;
import com.example.technobit.ui.colorDialog.ColorPickerSwatch;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.services.calendar.CalendarScopes;

// TODO: light the email preference??!?!
// TODO 2: Add preference for english/ita swapping?
// TODO 3: display dialog before revoke permission for email??
public class ToolsFragment extends PreferenceFragmentCompat {

    private SharedPreferences sharedPref;
    private Preference account_sel; // preference sulla scelta dell'account
    private Preference color_sel; // preference sulla scelta del colore
    private int color_selected; // colore selezionato
    private SwitchPreferenceCompat vibration;
    private int defaultColorValue;
    private GoogleSignInAccount googleAccount;
    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 7;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        //toolsViewModel = ViewModelProviders.of(this).get(ToolsViewModel.class);
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // salvo le shared preference per gestire lettura/salvataggio delle preferenze già inserite
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        // preference per l'account del fragment (XML)
        account_sel = findPreference("google_account");
        // preference per il colore degli eventi del fragment (XML)
        color_sel = findPreference("google_color");
        // preference per la vibrazione del fragment (XML)
        vibration = findPreference("notifications");

        String def_color = getResources().getString(R.string.default_color_str);
        defaultColorValue = Color.parseColor(def_color);
        // prendo il colore dalle shared preference, se non c'è viene settata al colore di default
        color_selected = sharedPref.getInt(getString(R.string.shared_saved_color), defaultColorValue);
        // setto l'icona del colore
        set_icon_color();

        // get the last account signIn, if exist
        googleAccount = GoogleSignIn.getLastSignedInAccount(getContext());

        // update the preference summary for the account
        set_summary_account();

        // event su "seleziona un account"
        account_sel.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(new Scope(CalendarScopes.CALENDAR)) // Scope to read/write calendar and drive
                        .requestEmail()
                        .build();

                // Build a GoogleSignInClient with the options specified by gso.
                mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);

                if(googleAccount == null){
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
                else{// if an account it's already signIn
                    // Revoke all access for the account
                    revokeAccess();
                    // signOut the account
                    signOut();
                }

                return true;
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

    private void set_summary_account(){
        if(googleAccount!=null) // if an account is signIn
            account_sel.setSummary(googleAccount.getEmail()); //set the account email as summary
        else
            account_sel.setSummary("");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            googleAccount = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            set_summary_account();


        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            // Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            set_summary_account();
        }
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        googleAccount = null;

                        // update the preference summary for the account
                        set_summary_account();
                    }
                });
    }

    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess();
    }

    private void color_choose(){
        // prendo tutta la lista di colori definita in resource
        int[] mColor = colorChoice();
        color_selected = sharedPref.getInt(getString(R.string.shared_saved_color), defaultColorValue);

        ColorPickerDialog colorcalendar = ColorPickerDialog.newInstance(
                R.string.color_picker_default_title, mColor,
                color_selected, 5,
                isTablet() ? ColorPickerDialog.SIZE_LARGE
                        : ColorPickerDialog.SIZE_SMALL);


        colorcalendar.setOnColorSelectedListener(colorcalendarListener);

        colorcalendar.show(this.getParentFragmentManager().beginTransaction(), "cal");
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