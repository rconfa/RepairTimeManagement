package com.example.technobit.utilities.notSendedData;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;

/*  This class store values that need to send into google drive.
    If there some error while sending or app crash it saves data into file.
    The file could have 3 syntax:

    1) if the image is already sent to drive
    1;EventTitle;EventDescription;eventDuration;eventEndTime;imageLink

    2) If the image is not sent to drive yet
    2;EventTitle;EventDescription;eventDuration;eventEndTime;imagePath

    3) If the app crash before the user sign and insert the description
    3;EventTitle;eventDuration;eventEndTime
 */
public class GoogleData {
    private static GoogleData instance;
    private String mEventTitle, mDescription, mImage; // title, descripion and attachment for the event
    private Long mEventDuration, mEventEnd; // duration and end time for the event



    private int mCase;


    // constructor with parameter
    private GoogleData(int mCase, String mEventTitle, String mDescription, String mAttachement,
                       Long mEventDuration, Long mEventEnd) {
        this.setCase(mCase);
        this.setEventTitle(mEventTitle);
        this.setDescription(mDescription);
        this.setImage(mAttachement);
        this.setEventDuration(mEventDuration);
        this.setEventEnd(mEventEnd);
    }

    public GoogleData() {
        this.mEventTitle = null;
        this.mDescription = null;
        this.mImage = null;
        this.mEventDuration = null;
        this.mEventEnd = null;
    }

    // GETTER
    public String getEventTitle() {
        return mEventTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getImage() {
        return mImage;
    }

    public Long getEventDuration() {
        return mEventDuration;
    }

    public Long getEventEnd() {
        return mEventEnd;
    }

    public int getCase() {
        return mCase;
    }



    // SETTER
    private void setEventTitle(String mEventTitle) {
        this.mEventTitle = mEventTitle;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    // i need to set the attachment also in a second moment
    public void setImage(String mAttachement) {
        this.mImage = mAttachement;
    }

    private void setEventDuration(Long mEventDuration) {
        this.mEventDuration = mEventDuration;
    }

    private void setEventEnd(Long mEventEnd) {
        this.mEventEnd = mEventEnd;
    }

    public void setCase(int mCase) {
        this.mCase = mCase;
    }

    @Override
    public String toString() {
        return  mCase + ";" + mEventTitle + ";" + mDescription + ";" + mEventDuration + ";" +
                mEventEnd + ";" + mImage;
    }

    // get the instance
    public static synchronized GoogleData getInstance(){
        if(instance==null){
            instance=new GoogleData();
        }
        return instance;
    }

    // initialize the instance with value
    public static synchronized GoogleData initialize(int mCase, String mEventTitle, String mDescription,
                                                     String mImage, Long mEventDuration,
                                                     Long mEventEnd){
        if(instance==null){
            instance=new GoogleData(mCase, mEventTitle, mDescription,mImage,mEventDuration,mEventEnd);
        }
        else{
            instance.setCase(mCase);
            instance.setEventTitle(mEventTitle);
            instance.setDescription(mDescription);
            instance.setImage(mImage);
            instance.setEventDuration(mEventDuration);
            instance.setEventEnd(mEventEnd);
        }
        return instance;
    }

    // reset the instance to null
    public static synchronized void reset(){
        instance = null;
    }


    public static synchronized boolean isInstanceNull(){
        return instance!=null;
    }


    public static synchronized boolean saveInstance(Context c) throws IOException {
        if(instance!=null){
            new FileGoogle().writeAllToFile(getInstance(),c);
        }

        reset();
        return true;
    }

    public ArrayList<GoogleData> getAll(Context mContext) throws IOException {
        return new FileGoogle().readFile(mContext);
    }


    public void readFromString(String line) {
        String[] unzippedData = line.split(";");

        this.mCase = Integer.parseInt(unzippedData[0]);
        this.mEventTitle = unzippedData[1];
        this.mEventDuration = Long.decode(unzippedData[3]);
        this.mEventEnd = Long.decode(unzippedData[4]);
        // if case == 3 then there are no description and no imageData
        if(mCase != 3) {
            this.mDescription = unzippedData[2];
            this.mImage = unzippedData[5];
        }
        else{
            this.mDescription = null;
            this.mImage = null;
        }
    }
}
