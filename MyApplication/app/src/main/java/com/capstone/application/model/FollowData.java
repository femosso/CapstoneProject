package com.capstone.application.model;

public class FollowData {
    private User user;
    private Teen teen;
    private boolean follow;

    public FollowData(User user, Teen teen, boolean follow) {
        this.user = user;
        this.teen = teen;
        this.follow = follow;
    }

    public User getUser() {
        return user;
    }

    public Teen getTeen() {
        return teen;
    }

    public boolean getFollow() {
        return follow;
    }

    public void setFollow(boolean follow) {
        this.follow = follow;
    }
}
