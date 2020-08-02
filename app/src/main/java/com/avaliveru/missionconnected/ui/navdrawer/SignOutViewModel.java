package com.avaliveru.missionconnected.ui.navdrawer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SignOutViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public void SignOutViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is SignOut fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
