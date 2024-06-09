package com.xiaohub.interactive.model.request;

public class TextContent extends Content {
    private String text;

    public TextContent() {
        super("text");
    }

    public TextContent(String text) {
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
