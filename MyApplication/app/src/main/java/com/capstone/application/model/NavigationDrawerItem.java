package com.capstone.application.model;

public class NavigationDrawerItem {
    private String mTitle;

    private int mIcon;

    // if logged via Facebook, this field should be set
    private String mProfileId;

    private int mCount = 0;

    public NavigationDrawerItem(String text, int icon) {
        mTitle = text;
        mIcon = icon;
    }

    public NavigationDrawerItem(String text, String profileId) {
        mTitle = text;
        mProfileId = profileId;
    }

    public String getText() {
        return mTitle;
    }

    public void setText(String text) {
        mTitle = text;
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

    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        mCount = count;
    }
}
