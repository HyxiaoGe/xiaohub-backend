package com.xiaohub.interactive.chat.dto.message;

import java.util.List;

/**
 * 文本消息体
 */
public class TextPayloadDto {
    private String model;
    private List<TextMessageDto> textMessageDtos;
    private Double temperature;
    private Integer maxTokens;
    private Boolean stream;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<TextMessageDto> getMessages() {
        return textMessageDtos;
    }

    public void setMessages(List<TextMessageDto> textMessageDtos) {
        this.textMessageDtos = textMessageDtos;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getMax_tokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }
}
