package com.example.demo.dto;

public class ChatRequest {
    private String pan;
    private String message;
    private String language;
    private String sessionId;

    public ChatRequest() {}

    public ChatRequest(String pan, String message, String language, String sessionId) {
        this.pan = pan;
        this.message = message;
        this.language = language;
        this.sessionId = sessionId;
    }

    public String getPan() { return pan; }
    public void setPan(String pan) { this.pan = pan; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}
