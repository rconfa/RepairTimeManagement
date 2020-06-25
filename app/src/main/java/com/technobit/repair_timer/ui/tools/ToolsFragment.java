package com.technobit.repair_timer.ui.tools;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.snackbar.Snackbar;
import com.technobit.repair_timer.R;
import com.technobit.repair_timer.service.google.GoogleUtility;
import com.technobit.repair_timer.ui.customize.dialog.ConfirmChoiceDialog;
import com.technobit.repair_timer.ui.customize.dialog.colorDialog.ColorPickerDialog;
import com.technobit.repair_timer.ui.customize.dialog.colorDialog.ColorPickerSwatch;
import com.technobit.repair_timer.ui.customize.dialog.colorDialog.ColorUtility;
import com.technobit.repair_timer.ui.customize.preference.RipPreference;
import com.technobit.repair_timer.utils.Constants;
import com.technobit.repair_timer.utils.SmartphoneControlUtility;

import static android.app.Activity.RESULT_OK;

// TODO: Add preference for english/ita swapping?
public class ToolsFragment extends PreferenceFragmentCompat implements ConfirmChoiceDialog.NoticeDialogListener{
    private static final String TAG = "ToolsFragment";
    private SharedPreferences mSharedPref;
    private RipPreference mPreferenceAccount; // preference sulla scelta dell'account
    private Preference mPreferenceColor; // preference sulla scelta del colore
    private int mColorSelected; // colore selezionato
    private SwitchPreferenceCompat mPreferenceVibration;
    private GoogleUtility mGoogleUtility;
    private int RC_SIGN_IN = 7;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        // display settings preference
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // get navigation argument
        boolean boolToRip = requireArguments().getBoolean("rip_account");

        // SharedPreference to read and save user preference
        mSharedPref = requireContext().getSharedPreferences(
                Constants.TOOLS_SHARED_PREF_FILENAME, Context.MODE_PRIVATE);

        // Get all preference from XML
        // XML preference for google account
        mPreferenceAccount = findPreference("google_account");

        if (mPreferenceAccount!=null)
            mPreferenceAccount.setToRip(boolToRip);

        // XML preference for google calendar color
        mPreferenceColor = findPreference("google_color");
        // XML preference for vibration
        mPreferenceVibration = findPreference("notifications");

        settingColorXMLPreference(); // add icon with the selected color near color preference

        // get google utility instance
        mGoogleUtility = GoogleUtility.getInstance();

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

        // parsing to int the default color
        int mDefaultColor = Color.parseColor(Constants.default_color);
        // Get the user selected color from the sharedPreference if exists otherwise set it as the default color
        mColorSelected = mSharedPref.getInt(Constants.TOOLS_SHARED_PREF_GOOGLE_COLOR, mDefaultColor);
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
            editor.putInt(Constants.TOOLS_SHARED_PREF_GOOGLE_COLOR, mColorSelected);
            editor.apply();
        }
    };


    // --- ACCOUNT PREFERENCE ---
    private void accountChooser() {
        boolean internet = new SmartphoneControlUtility(getContext()).checkInternetConnection();
        if(internet){
            // if there is no account connected start the intend for choose account and get permission
            if(mGoogleUtility.getAccount(getContext()) == null){
                // get signInClient and start the intent
                Intent signInIntent = mGoogleUtility.getSignInClient(getContext()).getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
            else{// if an account it's already signIn
                // listner for the dialog
                ConfirmChoiceDialog.NoticeDialogListener listener = this;
                // get the message from the resource
                String message = getString(R.string.dialog_confirm_signOut_message);
                // Create an instance of the dialog fragment and show it
                DialogFragment dialog = new ConfirmChoiceDialog(" ", message,listener);
                dialog.show(getParentFragmentManager(), TAG);
            }
        }
        else{
            showSnackbar(R.string.snackbar_internet_connection_error);
        }
    }

    // set the summary for the account preference
    private void setAccountSummary(){
        // get the last signin account
        GoogleSignInAccount account = mGoogleUtility.getAccount(getContext());
        if(account !=null)  // if an account is signIn
            mPreferenceAccount.setSummary(account.getEmail()); //set the account email as summary
        else
            mPreferenceAccount.setSummary("");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            setAccountSummary();
            // deleting the rip if setting
            mPreferenceAccount.deleteRip();
        }
        else{
            showSnackbar(R.string.snackbar_login_error);
        }
    }

    private void showSnackbar(int id_text_string){
        Snackbar snackbar = Snackbar.make(requireView(), id_text_string, Snackbar.LENGTH_LONG);
        snackbar.setTextColor(Color.WHITE);
        snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary))
                .setAction(getString(R.string.snackbar_close_btn), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });
        snackbar.show();
    }

    @Override
    public void onDialogPositiveClick() {
        // Revoke all access for the account
        mGoogleUtility.revokeAccess(getContext());
        // signOut the account
        mGoogleUtility.signOut(getContext());
        // update the preference summary for the account
        setAccountSummary();
    }

    @Override
    public void onDialogCancel() {

    }

    // --- VIBRATION PREFERENCE ---
    private void vibrationChange() {
        // Saving the choice into sharedPreference
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(Constants.TOOLS_SHARED_PREF_VIBRATION, !mPreferenceVibration.isChecked());
        editor.apply();
    }


}