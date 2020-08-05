package com.avaliveru.missionconnected.dataModels;

public class Users {
    private String user;
    private String imageUrl;
    private String uid;

    public Users(String user) {
        this.user = user;
    }

    public Users(String user, String imageUrl, String uid) {
        this.user = user;
        this.imageUrl = imageUrl;
        this.uid = uid;
    }

    public String getUser() {
        return user;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
