package com.capstone.application.model;

import java.util.List;

/**
 * Auxiliary class that carries useful information to load the list of teens in
 * Android app. It basically contains the list of teens requested plus the
 * information of the user that has requested the list. This information is
 * important to get, for example, which teens are still pending to confirm this
 * user's follow request.
 */
public class TeenListRequest {
    private List<User> teenList;

    private User requester;

    public TeenListRequest() {
    }

    public List<User> getTeenList() {
        return teenList;
    }

    public void setTeenList(List<User> teenList) {
        this.teenList = teenList;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }
}