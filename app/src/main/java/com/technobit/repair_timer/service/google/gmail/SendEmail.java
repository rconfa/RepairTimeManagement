package com.technobit.repair_timer.service.google.gmail;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.technobit.repair_timer.R;
import com.technobit.repair_timer.service.google.GoogleAsyncResponse;
import com.technobit.repair_timer.service.google.GoogleUtility;
import com.technobit.repair_timer.utils.SmartphoneControlUtility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendEmail  extends Thread {

    private Gmail mService; // google gmail service
    private GoogleAsyncResponse mdelegate;
    private Context mContext;
    private String mEmailTo, mEmailBody, mEmailSubject;

    public SendEmail(Context mContext, String mEmailTo, String mEmailSubject, String mEmailBody, GoogleAsyncResponse mdelegate) {
        this.mdelegate = mdelegate;
        this.mContext = mContext;
        this.mEmailTo = mEmailTo;
        this.mEmailSubject = mEmailSubject;
        this.mEmailBody = mEmailBody;

        // get account and credential from google Utility
        GoogleUtility gu = GoogleUtility.getInstance();
        GoogleSignInAccount account = gu.getAccount(mContext);
        GoogleAccountCredential credential = gu.getCredential(mContext);

        if(account != null && credential!=null) {
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            NetHttpTransport HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();
            credential.setSelectedAccount(account.getAccount()); // set the account
            // build the drive service
            mService = new Gmail.Builder(HTTP_TRANSPORT, jsonFactory, credential)
                    .setApplicationName(mContext.getString(R.string.app_name))
                    .build();
        }
        else
            mService = null;
    }

    private String sendEmail() throws IOException, MessagingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        createEmail().writeTo(baos);
        Message message = new Message();
        message.setRaw(Base64.encodeBase64URLSafeString(baos.toByteArray()));

        Gmail.Users.Messages.Send sender = mService.users().messages().send("me", message);

        sender.execute();

        // return true if there is no error
        return "true";
    }

    @Override
    public void run() {
        if(mService != null) {
            if(new SmartphoneControlUtility(mContext).checkInternetConnection()) {
                if (new SmartphoneControlUtility(mContext).emailIsValid(mEmailTo) && mEmailBody != null) {
                    try {
                        String result = sendEmail(); // send email
                        mdelegate.processFinish(result);
                    } catch (IOException | MessagingException e) {
                        e.printStackTrace();
                        mdelegate.processFinish("false");
                    }
                }
                else
                    mdelegate.processFinish("true");
                    // mdelegate.processFinish("noValidEmail");
            }
            else
                mdelegate.processFinish("noInternet");
        }
        else {
            mdelegate.processFinish("false");
        }
    }

    /**
     * Create a MimeMessage using the parameters provided.
     * @return the MimeMessage to be used to send email
     * @throws MessagingException
     */
    private MimeMessage createEmail() throws MessagingException {
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        mimeMessage.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(this.mEmailTo));
        mimeMessage.setSubject(this.mEmailSubject);
        mimeMessage.setText(this.mEmailBody);
        return mimeMessage;
    }



}
