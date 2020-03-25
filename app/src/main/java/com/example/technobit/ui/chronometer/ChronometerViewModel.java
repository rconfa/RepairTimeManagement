package com.example.technobit.ui.chronometer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ChronometerViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ChronometerViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is chronometer fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}