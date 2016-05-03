package com.example.henry.chatapplication;

/**
 * Created by hieu on 5/1/16.
 */
public class Response {

    private String result;
    private String username;
    private String sessionId;
    private int current;
    private int newest;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getNewest() {
        return newest;
    }

    public void setNewest(int newest) {
        this.newest = newest;
    }
}
