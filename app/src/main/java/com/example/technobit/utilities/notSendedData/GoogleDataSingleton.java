package com.example.technobit.utilities.notSendedData;

import android.content.Context;

import java.io.IOException;

public class GoogleDataSingleton {
    private static GoogleDataSingleton instance;
    private static GoogleData data;

    public GoogleDataSingleton() {
        data = null;
    }

    private void setData(GoogleData data) {
        this.data = data;
    }

    // get the instance
    public static synchronized GoogleDataSingleton getInstance(){
        if(instance==null){
            instance=new GoogleDataSingleton();
        }
        return instance;
    }

    // initialize the instance with value
    public static synchronized GoogleDataSingleton initialize(int mCase, String mEventTitle, String mDescription,
                                                     String mImage, Long mEventDuration,
                                                     Long mEventEnd){
        GoogleData temp = new GoogleData(mCase,mEventTitle,mDescription,mImage,mEventDuration,mEventEnd);


        if(instance==null){
            instance=new GoogleDataSingleton();
        }

        data = temp;
        return instance;
    }

    // reset the instance to null
    public static synchronized void reset(){
        instance = null;
        data = null;
    }


    public static synchronized boolean isInstanceNull(){
        return instance!=null;
    }


    public static synchronized boolean saveInstance(Context c) throws IOException {
        if(instance!=null){
            new FileGoogle().writeAllToFile(data,c);
        }

        reset();
        return true;
    }

    public static synchronized GoogleData getData(){
        return data;
    }
}
