package com.example.technobit.utilities.googleService;

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

import java.util.Arrays;

public class GoogleUtility {
    private Context mContext;
    private static GoogleUtility instance;

    public static synchronized GoogleUtility getInstance(Context mContext){
        if(instance==null){
            instance=new GoogleUtility(mContext);
        }
        return instance;
    }

    private GoogleUtility(Context mContext) {
        this.mContext = mContext;
    }

    // get Last signIn account
    public GoogleSignInAccount getAccount(){
        return GoogleSignIn.getLastSignedInAccount(mContext);
    }

    // get credential
    public GoogleAccountCredential getCredential(){
        return GoogleAccountCredential.usingOAuth2(mContext,
                Arrays.asList(CalendarScopes.CALENDAR, DriveScopes.DRIVE))
                .setBackOff(new ExponentialBackOff());
    }

    // sign out from account
    public void signOut() {
        getSignInClient().signOut();
    }

    // revoke all access for the account
    public void revokeAccess() {
        getSignInClient().revokeAccess();
    }

    // GoogleSignInClient
    public GoogleSignInClient getSignInClient(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(CalendarScopes.CALENDAR), new Scope(DriveScopes.DRIVE)) // Scope to read/write calendar and drive
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        return GoogleSignIn.getClient(mContext, gso);
    }


}
