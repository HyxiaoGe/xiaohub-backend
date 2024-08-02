package com.xiaohub.interactive.common.model;

public class BasicMessage {

    private Integer sessionId;

    private String type;

    private String content;

    public BasicMessage(Integer sessionId, String type, String content) {
        this.sessionId = sessionId;
        this.type = type;
        this.content = content;
    }

    public Integer getSessionId() {
        return sessionId;
    }

    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
