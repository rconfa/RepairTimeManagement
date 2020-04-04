package com.example.technobit.utilities;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;
import java.util.Collections;
import java.util.TimeZone;

// this class perform an insert in google calendar
public class AsyncInsertGoogleCalendar extends AsyncTask<String, Void, String> {
    private final Calendar mService;
    private String mEventTitle, mEventDescription;
    private long mStartMillis, mEndMillis;
    private int mEventColor;
    private static final JsonFactory mJsonFactory = JacksonFactory.getDefaultInstance();
    private AsyncResponse mdelegate = null;

    // constructor with parameters
    public AsyncInsertGoogleCalendar(String mEventTitle, String mEventDescription,
                                     long mStartMillis, long mEndMillis, int mEventColor,
                                     Context mContext, AsyncResponse delegate) {
        this.mEventTitle = mEventTitle;
        this.mEventDescription = mEventDescription;
        this.mStartMillis = mStartMillis;
        this.mEndMillis = mEndMillis;
        this.mEventColor = mEventColor;
        this.mdelegate = delegate;

        final NetHttpTransport HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(mContext,
                Collections.singleton(CalendarScopes.CALENDAR))
                .setBackOff(new ExponentialBackOff());

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(mContext);
        if(account != null) {
            credential.setSelectedAccount(account.getAccount());
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    HTTP_TRANSPORT, mJsonFactory, credential).setApplicationName("Technobit")
                    .build();
        }
        else
            mService = null;

    }

    private void insertEvent() throws IOException {
        Event event = new Event()
                .setSummary(mEventTitle)
                .setDescription(mEventDescription)
                .setColorId(Integer.toString(mEventColor));

        // overload reminders, I want no reminders!
        Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(null);
        event.setReminders(reminders);


        TimeZone tz = TimeZone.getDefault();

        java.util.Calendar c = java.util.Calendar.getInstance();
        DateTime endDate = new DateTime(c.getTime());

        c.setTimeInMillis(c.getTimeInMillis() - 9000);
        DateTime startDate = new DateTime(c.getTime());
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
            mService.events().insert(calendarId, event).execute();
    }

    @Override
    protected String doInBackground(String... strings) {
        if(mService != null) {
            try {
                insertEvent(); // insert the event into calendar
                return "true";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    @Override
    protected void onPostExecute (String result){
        // this is run on the main (UI) thread, after doInBackground returns
        mdelegate.processFinish(result);
    }
}