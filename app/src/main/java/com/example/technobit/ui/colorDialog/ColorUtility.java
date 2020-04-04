package com.example.technobit.ui.colorDialog;

import android.content.Context;
import android.graphics.Color;

import com.example.technobit.R;

public class ColorUtility {
    private Context mContext;

    public ColorUtility(Context mContext) {
        this.mContext = mContext;
    }

    public int[] getColorsArray(){
        // getting all the defined color from the resources
        String[] colorArray = mContext.getResources().getStringArray(R.array.default_color_choice_values);
        int[] colorChoices = new int[colorArray.length];
        // parse all color from string to int
        for (int i = 0; i < colorArray.length; i++) {
            colorChoices[i] = Color.parseColor(colorArray[i]);
        }

        return colorChoices;
    }
}
