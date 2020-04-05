package com.example.technobit.utilities.notSendedData;

/* write into a file all the data needed to send the event on google
   Syntax for file:
   Title;Description;StartTime;EndTime;ImagePath
   If the image path is empty the image is successfully send to drive yet.
 */

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;

public class DataToSend {
    String mEventTitle, mDescription, mImagePath; // title, descripion and signature image for the event
    Long mEventStart, mEventEnd; // start and end time

    // empty constructor
    public DataToSend() {
    }

    // constructor with parameters
    public DataToSend(String mEventTitle, String mDescription, String mImagePath, Long mEventStart, Long mEventEnd) {
        this.mEventTitle = mEventTitle;
        this.mDescription = mDescription;
        this.mImagePath = mImagePath;
        this.mEventStart = mEventStart;
        this.mEventEnd = mEventEnd;
    }

    @NonNull
    @Override
    public String toString() {
        return mEventTitle + ";" + mDescription + ";" + mEventStart + ";" + mEventEnd + ";" + mImagePath;
    }

    // restore all data from a string
    public void readFromString(String datas){
        String[] unzippedData = datas.split(";");
        this.mEventTitle = unzippedData[0];
        this.mDescription = unzippedData[1];
        this.mImagePath = unzippedData[4];
        this.mEventStart = Long.decode(unzippedData[2]);
        this.mEventEnd = Long.decode(unzippedData[3]);
    }

    public void saveOnFile(Context mContext) throws IOException {
        new FileNotSended().writeAllToFile(this,mContext);
    }

    public void setAllSended(Context mContext) throws IOException {
        new FileNotSended().delete(mContext);
    }

    public ArrayList<DataToSend> getAll(Context mContext) throws IOException {
        return new FileNotSended().readFile(mContext);
    }
}
