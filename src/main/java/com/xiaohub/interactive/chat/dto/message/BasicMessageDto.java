package com.xiaohub.interactive.chat.dto.message;

public class BasicMessageDto {

    private Integer sessionId;

    private String content;

    public BasicMessageDto(Integer sessionId, String content) {
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
