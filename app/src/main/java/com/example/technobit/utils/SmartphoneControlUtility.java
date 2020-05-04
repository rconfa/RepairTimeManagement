package com.example.technobit.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

// Class for smartphone utility like shaking, send notification...
public class SmartphoneControlUtility {
    private Context mContext;

    // constructor with parameters
    public SmartphoneControlUtility(Context mContext) {
        this.mContext = mContext;
    }

    // shake the smartphone
    public void shake() {
        // based of the Sdk version perform a shake of 600 millis.
        if (Build.VERSION.SDK_INT >= 26) {
            ((Vibrator) mContext.getSystemService(mContext.VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(600, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            ((Vibrator) mContext.getSystemService(mContext.VIBRATOR_SERVICE)).vibrate(600);
        }

    }

    public boolean checkInternetConnection(){
        ConnectivityManager manager =(ConnectivityManager) this.mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI){
                return true;
            }
            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE){
                return true;
            }
        }

        return false;
    }
}
