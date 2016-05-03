package com.example.henry.chatapplication;

/**
 * Created by hieu on 5/3/16.
 */
public class ChatEntry {

    private String username;
    private String message;

    public ChatEntry(EventEntry entry) {
        this.username = entry.getUsername();
        this.message = entry.getMessage();
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }
}
