package com.xiaohub.interactive.common.model;

import com.xiaohub.interactive.common.model.request.Content;
import com.xiaohub.interactive.common.util.JsonUtil;

import java.io.IOException;
import java.util.List;

public class Message {
    private String role;
    private List<Content> content;

    public Message() {
    }

    public Message(String role, List<Content> content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<Content> getContent() {
        return content;
    }

    public void setContent(List<Content> content) {
        this.content = content;
    }

    public void setContent(String content) throws IOException {
        this.content = JsonUtil.toObjectList(content, Content.class);
    }
}
