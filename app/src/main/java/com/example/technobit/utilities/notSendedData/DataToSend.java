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

/*  This dataToSend class create a file to store values that are not sended to google for exception error.
    The file has two syntax:
    1) isDriveSent = true, if the image is already sent to drive. I need to save only data to create the event on calendar
    true;EventTitle;EventDescription;eventDuration;eventEndTime;attachments
    2) isDriveSent = false, if the image is not sent to drive.
    false;EventTitle;EventDescription;eventDuration;eventEndTime;imagePath

 */
public class DataToSend {
    String mEventTitle, mDescription, mImageData; // title, descripion and signature image for the event
    Long mEventDuration, mEventEnd; // start and end time
    Boolean mIsDriveSent; // true if the image is already upload on drive, false otherwise

    // empty constructor
    public DataToSend() {
    }

    // constructor with parameters
    public DataToSend( Boolean isDriveSent, String mEventTitle, String mDescription, Long mEventDuration,
                      Long mEventEnd, String mImageData) {
        this.mEventTitle = mEventTitle;
        this.mDescription = mDescription;
        this.mImageData = mImageData;
        this.mEventDuration = mEventDuration;
        this.mEventEnd = mEventEnd;
        this.mIsDriveSent = isDriveSent;
    }

    @NonNull
    @Override
    public String toString() {
        return mIsDriveSent + ";" + mEventTitle + ";" + mDescription + ";" + mEventDuration + ";"
                + mEventEnd + ";" + mImageData;
    }

    // restore all data from a string
    public void readFromString(String datas){
        String[] unzippedData = datas.split(";");

        this.mIsDriveSent = Boolean.getBoolean(unzippedData[0]);
        this.mEventTitle = unzippedData[1];
        this.mDescription = unzippedData[2];
        this.mEventDuration = Long.decode(unzippedData[3]);
        this.mEventEnd = Long.decode(unzippedData[4]);
        if(!mIsDriveSent) // not sent to drive
            this.mImageData = unzippedData[5]; // image data = only the image path
        else
            this.mImageData = unzippedData[5] + ";" + unzippedData[6] + ";" + unzippedData[7]; // image data = all the attachments
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

    public String getEventTitle() {
        return mEventTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getImageData() {
        return mImageData;
    }

    public Long getEventDuration() {
        return mEventDuration;
    }

    public Long getEventEnd() {
        return mEventEnd;
    }

    public String getIsDriveSent() {
        return mIsDriveSent.toString();
    }
}
