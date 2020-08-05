package com.avaliveru.missionconnected.ui.navdrawer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SupportViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public SupportViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Mission Connect aims to facilitate communication between school clubs and make it easier for clubs to communicate with their members. A secondary goal is to make it much easier for schools to manage the club registration process, as we also include an easy way to tabulate club information.\n\n For further information, visit https://avaliveru.wixsite.com/missionconnect\nAny enquiries please contact missionconnectedapp@gmail.com");
    }

    public LiveData<String> getText() {
        return mText;
    }
}