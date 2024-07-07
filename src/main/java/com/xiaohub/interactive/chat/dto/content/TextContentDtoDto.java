package com.xiaohub.interactive.chat.dto.content;

public class TextContentDtoDto extends ContentDto {
    private String text;

    public TextContentDtoDto() {
        super("text");
    }

    public TextContentDtoDto(String text) {
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
