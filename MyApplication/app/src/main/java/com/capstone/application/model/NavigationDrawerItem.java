package com.capstone.application.model;

public class NavigationDrawerItem {

    private String mText;

    private int mIcon;

    // if logged via Facebook, this field should be set
    private String mProfileId;

    public NavigationDrawerItem(String text, int icon) {
        mText = text;
        mIcon = icon;
    }

    public NavigationDrawerItem(String text, String profileId) {
        mText = text;
        mProfileId = profileId;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public int getIcon() {
        return mIcon;
    }

    public void setIcon(int icon) {
        mIcon = icon;
    }

    public String getProfileId() {
        return mProfileId;
    }

    public void setProfileId(String profileId) {
        this.mProfileId = profileId;
    }
}
