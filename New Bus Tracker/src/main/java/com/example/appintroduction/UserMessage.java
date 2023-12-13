package com.example.appintroduction;

public class UserMessage {
    private String userEmail;
    private String message;

    public UserMessage() {
        // Empty constructor needed for Firebase
    }

    public UserMessage(String userEmail, String message) {
        this.userEmail = userEmail;
        this.message = message;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
