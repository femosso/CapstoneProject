package com.capstone.application.model;

public class LoginResponse {
    private JsonResponse jsonResponse;
    private User user;

    public LoginResponse() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public JsonResponse getJsonResponse() {
        return jsonResponse;
    }

    public void setJsonResponse(JsonResponse jsonResponse) {
        this.jsonResponse = jsonResponse;
    }
}
