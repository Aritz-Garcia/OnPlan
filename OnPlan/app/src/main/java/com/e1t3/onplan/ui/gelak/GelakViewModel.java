package com.e1t3.onplan.ui.gelak;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GelakViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public GelakViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}