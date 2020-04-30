package com.example.technobit.utilities.googleService.calendar;

import android.content.Context;

import com.example.technobit.R;
import com.example.technobit.utilities.googleService.GoogleAsyncResponse;
import com.example.technobit.utilities.googleService.GoogleUtility;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttachment;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class InsertToGoogleCalendar extends Thread {
    private final Calendar mService;
    private String mEventTitle, mEventDescription;
    private long mElapsedMillis;
    private Date mEndDate;
    private int mEventColor;
    private static final JsonFactory mJsonFactory = JacksonFactory.getDefaultInstance();
    private String mAttachments;
    private GoogleAsyncResponse mdelegate;

    public InsertToGoogleCalendar(String mEventTitle, String mEventDescription,
                                  Date mEndDate, long mEndMillis, int mEventColor,
                                  Context mContext,
                                  String mAttachments, GoogleAsyncResponse mdelegate) {
        this.mEventTitle = mEventTitle;
        this.mEventDescription = mEventDescription;
        this.mEndDate = mEndDate;
        this.mElapsedMillis = mEndMillis;
        this.mEventColor = mEventColor;
        this.mAttachments = mAttachments;
        this.mdelegate = mdelegate;

        // get account and credential from google Utility
        GoogleUtility gu = GoogleUtility.getInstance();
        GoogleSignInAccount account = gu.getAccount(mContext);
        GoogleAccountCredential credential = gu.getCredential(mContext);

        if(account != null && credential!=null) {
            NetHttpTransport HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();
            credential.setSelectedAccount(account.getAccount()); // set the account
            // build the calendar service
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    HTTP_TRANSPORT, mJsonFactory, credential)
                    .setApplicationName(mContext.getString(R.string.app_name))
                    .build();
        }
        else
            mService = null;
    }

    private void insertEvent() throws IOException {
        Event event = new Event()
                .setSummary(mEventTitle)
                .setColorId(Integer.toString(mEventColor));

        if(mEventDescription!=null)
            event.setDescription(mEventDescription);

        // overload reminders, I want no reminders!
        Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(null);
        event.setReminders(reminders);

        if(mAttachments!=null) {
            // create new attachments for the event
            List<EventAttachment> attachments = event.getAttachments();
            if (attachments == null) {
                attachments = new ArrayList<>();
            }

            attachments.add(new EventAttachment()
                    .setFileUrl(mAttachments)
            );

            event.setAttachments(attachments);
        }

        TimeZone tz = TimeZone.getDefault();

        DateTime endDate = new DateTime(mEndDate.getTime());


        mEndDate.setTime(mEndDate.getTime() - mElapsedMillis);
        DateTime startDate = new DateTime(mEndDate.getTime());

        EventDateTime start = new EventDateTime()
                .setDateTime(startDate)
                .setTimeZone(tz.getID());
        event.setStart(start);


        EventDateTime end = new EventDateTime()
                .setDateTime(endDate)
                .setTimeZone(tz.getID());
        event.setEnd(end);

        String calendarId = "primary";
        //event.send
        if(mService!=null)
            mService.events().insert(calendarId, event).setSupportsAttachments(true)
                    .execute();
    }


    @Override
    public void run() {
        if(mService != null) {
            try {
                insertEvent(); // insert the event into calendar
                mdelegate.processFinish("true");
            } catch (IOException e) {
                e.printStackTrace();
                mdelegate.processFinish("false");
            }
        }
        else {
            mdelegate.processFinish("false");
        }
    }

}
