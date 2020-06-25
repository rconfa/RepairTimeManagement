package com.technobit.repair_timer.service.google.drive;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.technobit.repair_timer.R;
import com.technobit.repair_timer.service.google.GoogleAsyncResponse;
import com.technobit.repair_timer.service.google.GoogleUtility;
import com.technobit.repair_timer.utils.SmartphoneControlUtility;

import java.io.IOException;

// this class perform an image upload in google drive
public class InsertToGoogleDrive extends Thread {

    private Drive mService; // google drive service
    private java.io.File mFilepath; // local image filepath
    private GoogleAsyncResponse mdelegate;
    private Context mContext;
    public InsertToGoogleDrive(java.io.File mFilepath, Context mContext,
                                  GoogleAsyncResponse mdelegate) {
        this.mFilepath = mFilepath;
        this.mdelegate = mdelegate;
        this.mContext = mContext;

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
        fileMetadata.setName(this.mFilepath.getName());
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
            if(new SmartphoneControlUtility(mContext).checkInternetConnection()) {
                try {
                    String result = insertImage(); // upload an image into google drive
                    mdelegate.processFinish(result);
                } catch (IOException e) {
                    e.printStackTrace();
                    mdelegate.processFinish("false");
                }
            }
            else
                mdelegate.processFinish("noInternet");
        }
        else {
            mdelegate.processFinish("false");
        }
    }

}
