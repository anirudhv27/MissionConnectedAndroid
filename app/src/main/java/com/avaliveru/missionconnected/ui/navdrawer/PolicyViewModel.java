package com.avaliveru.missionconnected.ui.navdrawer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PolicyViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public PolicyViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Mission Connect is a free app intended to be used as is.\n Please visit https://avaliveru.wixsite.com/missionconnect/privacy-policy for further information on Privacy Policy");
    }

    public LiveData<String> getText() {
        return mText;
    }
}