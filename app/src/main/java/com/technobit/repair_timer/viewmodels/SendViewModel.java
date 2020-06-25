package com.technobit.repair_timer.viewmodels;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.technobit.repair_timer.repositories.dataNotSent.GoogleData;
import com.technobit.repair_timer.repositories.dataNotSent.GoogleDataRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class SendViewModel extends ViewModel {
    // MutableLiveData of all contacts
    private MutableLiveData<ArrayList<GoogleData>> datas;
    private MutableLiveData<Integer> progress;

    public MutableLiveData<ArrayList<GoogleData>> getDatas(Context mContext){
        // not check if datas is null because I need to update the arrayList each time
        // the user ask for send fragment!!
        datas = new MutableLiveData<>();
        try {
            datas.setValue(new GoogleDataRepository().getAll(mContext));
        } catch (IOException e) {
            datas = new MutableLiveData<>(); // restore all if there is some error
        }

        return datas;
    }

    public ArrayList<GoogleData> getListValue(){
        if(datas != null){
            return datas.getValue();
        }

        return null;
    }

    public void updateDatas(Context mContext){
        try {
            new GoogleDataRepository().writeAll(datas.getValue(), mContext);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GoogleData getSingleData(int index){
        if(datas != null) {
            return datas.getValue().get(index);
        }

        return null;
    }

    public int getDataSize(){
        if(datas != null) {
            return datas.getValue().size();
        }

        return 0;
    }

    public void clearNullData(){
        if(datas != null) {
            datas.getValue().removeAll(Collections.singleton(null));
        }
    }

    public void updateSingleData(int index, GoogleData d){
        if(datas.getValue() != null) {
            datas.getValue().set(index, d);
        }
    }

    public MutableLiveData<Integer> getProgress() {
        // not check if datas is null because I need to update the arrayList each time
        // the user ask for send fragment!!
        progress = new MutableLiveData<>();
        progress.setValue(0);

        return progress;
    }

    public void updateProgress(int value){
        if(progress != null)
            progress.setValue(value);
    }

}
