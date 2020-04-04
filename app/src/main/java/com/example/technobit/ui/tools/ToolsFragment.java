package com.example.technobit.ui.tools;

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

import com.example.technobit.R;
import com.example.technobit.ui.colorDialog.ColorPickerDialog;
import com.example.technobit.ui.colorDialog.ColorPickerSwatch;
import com.example.technobit.ui.colorDialog.ColorUtility;
import com.example.technobit.utilities.googleService.GoogleUtility;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

// TODO: light the email preference??!?!
// TODO 2: Add preference for english/ita swapping?
// TODO 3: display dialog before revoke permission for email??
public class ToolsFragment extends PreferenceFragmentCompat {

    private SharedPreferences mSharedPref;
    private Preference mPreferenceAccount; // preference sulla scelta dell'account
    private Preference mPreferenceColor; // preference sulla scelta del colore
    private int mColorSelected; // colore selezionato
    private SwitchPreferenceCompat mPreferenceVibration;
    private GoogleUtility mGoogleUtility;

    private int RC_SIGN_IN = 7;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        //toolsViewModel = ViewModelProviders.of(this).get(ToolsViewModel.class);

        // display settings preference
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // SharedPreference to read and save user preference
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Get all preference from XML
        // XML preference for google account
        mPreferenceAccount = findPreference("google_account");
        // XML preference for google calendar color
        mPreferenceColor = findPreference("google_color");
        // XML preference for vibration
        mPreferenceVibration = findPreference("notifications");

        settingColorXMLPreference(); // add icon with the selected color near color preference

        // get google utility instance
        mGoogleUtility = GoogleUtility.getInstance(getContext());

        // update the preference summary for the account
        setAccountSummary();

        // onclick event for account preference
        mPreferenceAccount.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                // if there isn't connected account start intent for connected a new account
                // otherwise singout the last one.
                accountChooser();
                return true;
            }
        });

        // On click event for choosing color
        mPreferenceColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                colorChooser(); // Method to display the dialog
                return true;
            }
        });

        // On change event for vibration
        mPreferenceVibration.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                vibrationChange();
                return true;
            }
        });
    }

    // --- COLOR PREFERENCE ---

    // method for setting the xml color preference
    private void settingColorXMLPreference() {
        // Setting the icon for color preference with the selected one or the default one if no one is selected yet
        // getting the default color from resource
        String def_color = getResources().getString(R.string.default_color_str);
        // parsing to int the default color
        int mDefaultColor = Color.parseColor(def_color);
        // Get the user selected color from the sharedPreference if exists otherwise set it as the default color
        mColorSelected = mSharedPref.getInt(getString(R.string.shared_saved_color), mDefaultColor);
        // Add an icon near the xml color preference to display the selected color
        setColorIcon();
    }

    private void colorChooser(){
        ColorUtility colorUtility = new ColorUtility(getContext());
        // get all color list defined
        int[] mColor = colorUtility.getColorsArray();

        // create the display dialog
        ColorPickerDialog colorCalendar = ColorPickerDialog.newInstance(
                R.string.color_picker_default_title, mColor,
                mColorSelected, 5,
                isTablet() ? ColorPickerDialog.SIZE_LARGE
                        : ColorPickerDialog.SIZE_SMALL);

        // set listener on color selection
        colorCalendar.setOnColorSelectedListener(colorCalendarListener);

        // show the dialog
        colorCalendar.show(this.getParentFragmentManager().beginTransaction(), "colorPickerDialog");
    }

    // add icon for color preference
    private void setColorIcon(){
        // get white circle image
        Drawable d = getResources().getDrawable(R.drawable.cerchio);

        // Change the color for the image with the selected ones
        PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(mColorSelected, PorterDuff.Mode.SRC_ATOP);

        // setting color filter for the drawable
        d.setColorFilter(colorFilter);
        mPreferenceColor.setIcon(d); // add the icon near xml preference

    }

    // True if the application is running on tablet, false otherwise
    private boolean isTablet() {
        return (getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    // Event on color choosing
    private ColorPickerSwatch.OnColorSelectedListener colorCalendarListener = new ColorPickerSwatch.OnColorSelectedListener(){
        @Override
        public void onColorSelected(int color) {
            mColorSelected = color; // update the selected color

            // update the color icon
            setColorIcon();
            // write the selected color on sharedPreference
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putInt(getString(R.string.shared_saved_color), mColorSelected);
            editor.apply();
        }
    };


    // --- ACCOUNT PREFERENCE ---

    private void accountChooser() {
        // if there is no account connected start the intend for choose account and get permission
        if(mGoogleUtility.getAccount() == null){
            // get signInClient and start the intent
            Intent signInIntent = mGoogleUtility.getSignInClient().getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
        else{// if an account it's already signIn
            // Revoke all access for the account
            mGoogleUtility.revokeAccess();
            // signOut the account
            mGoogleUtility.signOut();
            // update the preference summary for the account
            setAccountSummary();
        }
    }

    // set the summary for the account preference
    private void setAccountSummary(){
        // get the last signin account
        GoogleSignInAccount account = mGoogleUtility.getAccount();
        if(account !=null) // if an account is signIn
            mPreferenceAccount.setSummary(account.getEmail()); //set the account email as summary
        else
            mPreferenceAccount.setSummary("");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            setAccountSummary();
        }
    }


    // --- VIBRATION PREFERENCE ---
    private void vibrationChange() {
        // Saving the choice into sharedPreference
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(getString(R.string.shared_vibration), mPreferenceVibration.isChecked());
        editor.apply();
    }

}