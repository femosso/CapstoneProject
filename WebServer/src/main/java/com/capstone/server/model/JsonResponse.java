
package com.capstone.server.model;

import org.springframework.http.HttpStatus;

public class JsonResponse {

    private HttpStatus status;
    private String message = "";

    public JsonResponse(HttpStatus ok, String message) {
        this.status = ok;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

}
