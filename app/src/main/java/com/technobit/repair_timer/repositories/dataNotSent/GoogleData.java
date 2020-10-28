package com.technobit.repair_timer.repositories.dataNotSent;

import androidx.annotation.NonNull;

import com.technobit.repair_timer.ui.contact.Card;

/*  This class store values that need to send into google drive/calendar.
    If there some error while sending or app crash it saves data into file.
    The file could have 3 syntax:

    1) if the image is already sent to drive but missing calendar + email
    1;EventTitle;EventDescription;eventDuration;eventEndTime;imageLink

    2) If the image is not sent to drive yet and also missing calendar + email
    2;EventTitle;EventDescription;eventDuration;eventEndTime;imagePath

    3) If the app crash before the user sign and insert the description. Need to send only to calendar
    3;EventTitle;eventDuration;eventEndTime

    4) Missing only to send email
    4;EventDescription;eventEndTime  --> riesco a recuperare data e ora da li altrimenti salvo subj!
 */
public class GoogleData {
    private String mEventTitle, mDescription, mImage; // title, descripion and attachment for the event
    private Long mEventDuration, mEventEnd; // duration and end time for the event
    private int mCase;
    private String mEmail;

    // constructor with parameter
    protected GoogleData(int mCase, String mEventTitle, String mDescription, String mAttachment,
                       Long mEventDuration, Long mEventEnd, String mEmail) {
        this.setCase(mCase);
        this.setEventTitle(mEventTitle);
        this.setDescription(mDescription);
        this.setImage(mAttachment);
        this.setEventDuration(mEventDuration);
        this.setEventEnd(mEventEnd);
        this.mEmail = mEmail;
    }

    public GoogleData() {
        this.mEventTitle = null;
        this.mDescription = null;
        this.mImage = null;
        this.mEventDuration = null;
        this.mEventEnd = null;
        this.mEmail = null;
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

    public String getEmail() {
        return mEmail;
    }

    // SETTER
    public void setEventTitle(String mEventTitle) {
        this.mEventTitle = mEventTitle;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    // i need to set the attachment also in a second moment
    public void setImage(String mAttachement) {
        this.mImage = mAttachement;
    }

    public void setEventDuration(Long mEventDuration) {
        this.mEventDuration = mEventDuration;
    }

    private void setEventEnd(Long mEventEnd) {
        this.mEventEnd = mEventEnd;
    }

    public void setCase(int mCase) {
        this.mCase = mCase;
    }


    @NonNull
    @Override
    public String toString() {
        return  mCase + ";" + mEventTitle + ";" + mDescription + ";" + mEventDuration + ";" +
                mEventEnd + ";" + mImage + ";" + mEmail;
    }


    protected GoogleData readFromString(String line) {
        GoogleData toRet = new GoogleData();
        String[] unzippedData = line.split(";");

        toRet.mCase = Integer.parseInt(unzippedData[0]);
        toRet.mEventEnd = Long.decode(unzippedData[4]);
        if(unzippedData.length==7)
            toRet.mEmail = unzippedData[6];
        else
            toRet.mEmail = "";
        // in case 4 there is no event title and duration
        if(toRet.mCase != 4) {
            toRet.mEventTitle = unzippedData[1];
            toRet.mEventDuration = Long.decode(unzippedData[3]);
        }
        // if case == 3 then there are no description and no imageData
        if(toRet.mCase != 3) {
            toRet.mDescription = unzippedData[2];
            toRet.mImage = unzippedData[5];
        }



        return toRet;
    }



}
