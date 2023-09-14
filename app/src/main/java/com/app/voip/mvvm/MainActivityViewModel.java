package com.app.voip.mvvm;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.app.voip.BR;

public class MainActivityViewModel extends BaseObservable {

    private String title;
    private String joinLeaveButtonText;
    private String muteUnMuteButtonText;
    private String networkStatus;

    //Default Public Constructor
    public MainActivityViewModel() {}

    public MainActivityViewModel(String title, String joinLeaveButtonText,
                               String muteUnMuteButtonText, String networkStatus)
    {
        this.title = title;
        this.joinLeaveButtonText = joinLeaveButtonText;
        this.muteUnMuteButtonText = muteUnMuteButtonText;
        this.networkStatus = networkStatus;
    }

    @Bindable
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    public String getJoinLeaveButtonText() {
        return joinLeaveButtonText;
    }

    public void setJoinLeaveButtonText(String joinLeaveButtonText) {
        this.joinLeaveButtonText = joinLeaveButtonText;
        notifyPropertyChanged(BR.joinLeaveButtonText);
    }

    @Bindable
    public String getMuteUnMuteButtonText() {
        return muteUnMuteButtonText;
    }

    public void setMuteUnMuteButtonText(String muteUnMuteButtonText) {
        this.muteUnMuteButtonText = muteUnMuteButtonText;
        notifyPropertyChanged(BR.muteUnMuteButtonText);
    }

    @Bindable
    public String getNetworkStatus() {
        return networkStatus;
    }

    public void setNetworkStatus(String networkStatus) {
        this.networkStatus = networkStatus;
        notifyPropertyChanged(BR.networkStatus);
    }
}
