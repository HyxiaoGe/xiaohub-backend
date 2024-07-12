package com.xiaohub.interactive.chat.dto.content;

public class TextChatContentDto extends ChatContentDto {
    private String text;

    public TextChatContentDto() {
        super("text");
    }

    public TextChatContentDto(String text) {
        super("text");
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
