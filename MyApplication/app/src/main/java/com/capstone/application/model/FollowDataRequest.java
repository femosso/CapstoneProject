package com.capstone.application.model;

public class FollowDataRequest {
    private User user;

    private Teen teen;

    private boolean follow;

    public FollowDataRequest() {
    }

    public FollowDataRequest(User user, Teen teen, boolean follow) {
        this.user = user;
        this.teen = teen;
        this.follow = follow;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Teen getTeen() {
        return teen;
    }

    public void setTeen(Teen teen) {
        this.teen = teen;
    }

    public boolean getFollow() {
        return follow;
    }

    public void setFollow(boolean follow) {
        this.follow = follow;
    }
}
