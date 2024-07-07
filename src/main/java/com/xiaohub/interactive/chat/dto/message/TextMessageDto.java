package com.xiaohub.interactive.chat.dto.message;

import com.xiaohub.interactive.chat.dto.content.ContentDto;
import com.xiaohub.util.JsonUtil;

import java.io.IOException;
import java.util.List;

public class TextMessageDto {
    private String role;
    private List<ContentDto> contentDto;

    public TextMessageDto() {
    }

    public TextMessageDto(String role, List<ContentDto> contentDto) {
        this.role = role;
        this.contentDto = contentDto;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<ContentDto> getContent() {
        return contentDto;
    }

    public void setContent(List<ContentDto> contentDto) {
        this.contentDto = contentDto;
    }

    public void setContent(String content) throws IOException {
        this.contentDto = JsonUtil.toObjectList(content, ContentDto.class);
    }
}
