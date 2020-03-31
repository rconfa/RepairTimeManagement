package com.example.technobit.utilities;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

// Class for smartphone utility like shaking, send notification...
public class SmartphoneControlUtility {
    private Context mContext;
    private boolean mVibrationEnable;

    // constructor with parameters
    public SmartphoneControlUtility(Context mContext, boolean canVib) {
        this.mContext = mContext;
        this.mVibrationEnable = canVib;
    }

    // getter
    public boolean isVibrationEnable() {
        return mVibrationEnable;
    }

    // setter
    public void setVibrationEnable(boolean mVibrationEnable) {
        this.mVibrationEnable = mVibrationEnable;
    }

    // shake the smartphone
    private void shakeIt() {
        // check if the vibration is enable
        if (mVibrationEnable) {
            // based of the Sdk version perform a shake of 600 millis.
            if (Build.VERSION.SDK_INT >= 26) {
                ((Vibrator) mContext.getSystemService(mContext.VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(600, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                ((Vibrator) mContext.getSystemService(mContext.VIBRATOR_SERVICE)).vibrate(600);
            }
        }
    }
}
