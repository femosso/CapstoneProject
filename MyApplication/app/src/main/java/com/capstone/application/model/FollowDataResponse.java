package com.capstone.application.model;

import java.util.List;

public class FollowDataResponse {
    private JsonResponse response;

    private List<User> followRequestList;

    public FollowDataResponse() {
    }

    public JsonResponse getResponse() {
        return response;
    }

    public void setResponse(JsonResponse response) {
        this.response = response;
    }

    public List<User> getFollowRequestList() {
        return followRequestList;
    }

    public void setFollowRequestList(List<User> followRequestList) {
        this.followRequestList = followRequestList;
    }
}