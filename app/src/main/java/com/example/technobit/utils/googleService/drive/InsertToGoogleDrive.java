package com.example.technobit.utils.googleService.drive;

import android.content.Context;

import com.example.technobit.R;
import com.example.technobit.utils.googleService.GoogleAsyncResponse;
import com.example.technobit.utils.googleService.GoogleUtility;
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
public class InsertToGoogleDrive extends Thread {

    private Drive mService; // google drive service
    private String mImageName; // name for the image
    private java.io.File mFilepath; // local image filepath
    private GoogleAsyncResponse mdelegate;

    public InsertToGoogleDrive(String mImageName, java.io.File mFilepath, Context mContext,
                                  GoogleAsyncResponse mdelegate) {
        this.mImageName = mImageName;
        this.mFilepath = mFilepath;
        this.mdelegate = mdelegate;

        // get account and credential from google Utility
        GoogleUtility gu = GoogleUtility.getInstance();
        GoogleSignInAccount account = gu.getAccount(mContext);
        GoogleAccountCredential credential = gu.getCredential(mContext);

        if(account != null && credential!=null) {
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            NetHttpTransport HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();
            credential.setSelectedAccount(account.getAccount()); // set the account
            // build the drive service
            mService = new Drive.Builder(HTTP_TRANSPORT, jsonFactory, credential)
                    .setApplicationName(mContext.getString(R.string.app_name))
                    .build();
        }
        else
            mService = null;
    }

    private String insertImage() throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(mImageName);
        FileContent mediaContent = new FileContent("image/jpeg", mFilepath);

        Drive.Files.Create fileDrive = mService.files().create(fileMetadata, mediaContent);

        // fileDrive.getMediaHttpUploader().setProgressListener(new CustomProgressListener());

        File result = fileDrive.setFields("webViewLink").execute();

        // return the information that I need for add attachments on google calendar
        return result.getWebViewLink();
    }

    @Override
    public void run() {
        if(mService != null) {
            try {
                String result = insertImage(); // upload an image into google drive
                mdelegate.processFinish(result);
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
