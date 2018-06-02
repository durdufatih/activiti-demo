package com.arel.activiti.model;

public class GeneralResponse {
    private String message;
    private boolean status;

    public GeneralResponse(String message, boolean status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
