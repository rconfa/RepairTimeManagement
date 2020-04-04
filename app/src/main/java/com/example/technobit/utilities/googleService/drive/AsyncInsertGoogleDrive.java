package com.example.technobit.utilities.googleService.drive;

import android.content.Context;
import android.os.AsyncTask;

import com.example.technobit.utilities.googleService.GoogleAsyncResponse;
import com.example.technobit.utilities.googleService.GoogleUtility;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;

// this class perform an image upload in google drive
public class AsyncInsertGoogleDrive extends AsyncTask<String, Void, String> {
    private static final JsonFactory mJsonFactory = JacksonFactory.getDefaultInstance();
    Drive mService; // google drive service
    String mImageName; // name for the image
    java.io.File mFilepath; // local image filepath
    private GoogleAsyncResponse mdelegate = null;

    public AsyncInsertGoogleDrive(String mImageName, java.io.File mFilepath, Context mContext,
                                  GoogleAsyncResponse mdelegate) {
        this.mImageName = mImageName;
        this.mFilepath = mFilepath;
        this.mdelegate = mdelegate;

        // get account and credential from google Utility
        GoogleUtility gu = GoogleUtility.getInstance(mContext);
        GoogleSignInAccount account = gu.getAccount();
        GoogleAccountCredential credential = gu.getCredential();

        if(account != null && credential!=null) {
            NetHttpTransport HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();
            credential.setSelectedAccount(account.getAccount()); // set the account
            // build the drive service
            mService = new Drive.Builder(HTTP_TRANSPORT, mJsonFactory, credential)
                    .setApplicationName("Technobit")
                    .build();
        }
        else
            mService = null;
    }

    private String insertImage() throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(mImageName);
        FileContent mediaContent = new FileContent("image/jpeg", mFilepath);
        File file = mService.files().create(fileMetadata, mediaContent)
                .setFields("mimeType, name, webViewLink")
                .execute();

        // return the information that I need for add attachments on google calendar
        return file.getWebViewLink() +";"+file.getMimeType()+";"+file.getName();
    }


    @Override
    protected String doInBackground(String... strings) {
        if(mService != null) {
            try {
                return insertImage(); // upload an image into google drive
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
