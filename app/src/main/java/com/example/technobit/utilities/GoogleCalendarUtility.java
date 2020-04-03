package com.example.technobit.utilities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import java.util.TimeZone;

public class GoogleCalendarUtility {
    private String mEventTitle, mEventDescription, mSelectedEmail;
    private long mStartMillis, mEndMillis;
    private int mEventColor;
    private Fragment mCallingFragment;
    private Context mContext;

    // constructor with parameters
    public GoogleCalendarUtility(String mEventTitle, String mEventDescription, String mSelectedEmail,
                                 long mStartMillis, long mEndMillis, int mEventColor,
                                 Fragment mCallingFragment) {
        this.mEventTitle = mEventTitle;
        this.mEventDescription = mEventDescription;
        this.mSelectedEmail = mSelectedEmail;
        this.mStartMillis = mStartMillis;
        this.mEndMillis = mEndMillis;
        this.mEventColor = mEventColor;
        this.mCallingFragment = mCallingFragment;
        this.mContext = this.mCallingFragment.getContext();
    }

    public boolean sendOnCalendar() {
        ContentResolver cr = mContext.getContentResolver();
        String calID = searchID(cr); // search for the calendar id

        if (!calID.equals("-1")) {
            TimeZone tz = TimeZone.getDefault();

            ContentValues values = new ContentValues();

            // mettere controllo id!=-1
            values.put(CalendarContract.Events.DTSTART, mStartMillis);
            values.put(CalendarContract.Events.DTEND, mEndMillis);
            values.put(CalendarContract.Events.TITLE, mEventTitle);
            values.put(CalendarContract.Events.DESCRIPTION, mEventDescription);
            values.put(CalendarContract.Events.CALENDAR_ID, calID);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, tz.getID());
            values.put(CalendarContract.Events.EVENT_COLOR_KEY, mEventColor);

            //Try to add the event on calendar
            try {
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_CALENDAR) !=
                        PackageManager.PERMISSION_GRANTED) {
                    mCallingFragment.requestPermissions(new String[]{Manifest.permission.WRITE_CALENDAR}, 0);
                    return false;
                } else {
                    Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
                    return true;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }


    private String searchID(ContentResolver cr) {
        // Search the calendar id
        String projection[] = {"_id"}; // richiedo l'id del calendario

        // Set the args in base the email address
        String[] selectionArgs = new String[]{this.mSelectedEmail};

        // Execute the query requesting the field name like the one specified in the selectionArgs
        Cursor managedCursor = cr.query(Uri.parse("content://com.android.calendar/calendars"),
                projection, "calendar_displayName=?", selectionArgs, null);

        // Scan all the calendars
        String calID = "-1";

        try {
            managedCursor.moveToFirst();// Try to handle the first element

            calID = managedCursor.getString(0); // getting is id

            managedCursor.close();
        } catch (NullPointerException ne) {
            return "-1";
        } catch (Exception e) {
            return "-1";
        }
        return calID;
    }

    public String getSelectedEmail() {
        return mSelectedEmail;
    }

    public void setSelectedEmail(String mSelectedEmail) {
        this.mSelectedEmail = mSelectedEmail;
    }
}