package com.e1t3.onplan.ui.kontaktua;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class KontaktuaViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public KontaktuaViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is contact fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}