package com.capstone.application.model;

import org.springframework.http.HttpStatus;

public class JsonResponse {

    private HttpStatus status;
    private String message;

    public JsonResponse() {
    }

    public JsonResponse(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}