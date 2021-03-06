package com.technobit.repair_timer.service.google;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.gmail.GmailScopes;

import java.util.Arrays;

public class GoogleUtility {

    private static GoogleUtility instance;

    public static synchronized GoogleUtility getInstance(){
        if(instance==null){
            instance=new GoogleUtility();
        }
        return instance;
    }

    // get Last signIn account
    public GoogleSignInAccount getAccount(Context mContext){
        return GoogleSignIn.getLastSignedInAccount(mContext);
    }

    // get credential
    public GoogleAccountCredential getCredential(Context mContext){
        return GoogleAccountCredential.usingOAuth2(mContext,
                Arrays.asList(CalendarScopes.CALENDAR, DriveScopes.DRIVE, GmailScopes.GMAIL_SEND))
                .setBackOff(new ExponentialBackOff());
    }

    // sign out from account
    public void signOut(Context mContext) {
        getSignInClient(mContext).signOut();
    }

    // revoke all access for the account
    public void revokeAccess(Context mContext) {
        getSignInClient(mContext).revokeAccess();
    }

    // GoogleSignInClient
    public GoogleSignInClient getSignInClient(Context mContext){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // Scope to read/write calendar, drive and send email
                .requestScopes(new Scope(CalendarScopes.CALENDAR), new Scope(DriveScopes.DRIVE),
                        new Scope(GmailScopes.GMAIL_SEND))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        return GoogleSignIn.getClient(mContext, gso);
    }


}
