package com.example.technobit.utilities.googleService;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.drive.DriveScopes;

import java.util.Arrays;

public class GoogleUtility {
    private Context mContext;
    private static GoogleUtility instance;

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

    public static synchronized GoogleUtility getInstance(Context mContext){
        if(instance==null){
            instance=new GoogleUtility(mContext);
        }
        return instance;
    }

}
