package com.xiaohub.interactive.chat.dto.content;

public class TextChatContentDtoDto extends ChatContentDto {
    private String text;

    public TextChatContentDtoDto() {
        super("text");
    }

    public TextChatContentDtoDto(String text) {
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
