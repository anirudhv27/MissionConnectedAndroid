package com.avaliveru.missionconnected.ui.clubs;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ClubsTabViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ClubsTabViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is clubs fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}