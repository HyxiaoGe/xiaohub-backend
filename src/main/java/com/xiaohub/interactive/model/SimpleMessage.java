package com.xiaohub.interactive.model;

public class SimpleMessage {

    private Integer sessionId;

    private String content;

    public SimpleMessage(Integer sessionId, String content) {
        this.sessionId = sessionId;
        this.content = content;
    }

    public Integer getSessionId() {
        return sessionId;
    }

    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
