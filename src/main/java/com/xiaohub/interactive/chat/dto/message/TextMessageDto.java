package com.xiaohub.interactive.chat.dto.message;

import com.xiaohub.interactive.chat.dto.content.ChatContentDto;
import com.xiaohub.util.JsonUtil;

import java.io.IOException;
import java.util.List;

public class TextMessageDto {
    private String role;
    private List<ChatContentDto> chatContentDto;

    public TextMessageDto() {
    }

    public TextMessageDto(String role, List<ChatContentDto> chatContentDto) {
        this.role = role;
        this.chatContentDto = chatContentDto;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<ChatContentDto> getContent() {
        return chatContentDto;
    }

    public void setContent(List<ChatContentDto> chatContentDto) {
        this.chatContentDto = chatContentDto;
    }

    public void setContent(String content) throws IOException {
        this.chatContentDto = JsonUtil.toObjectList(content, ChatContentDto.class);
    }
}
