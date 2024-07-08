package com.xiaohub.interactive.image.dto.payload;

/**
 * 图片消息体
 */
public class ImagePayloadDto {
    private String prompt;
    private String model;
    private Integer n;
    private String size;
//    private String quality;
//    private String response_format;
//    private String style;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Integer getN() {
        return n;
    }

    public void setN(Integer n) {
        this.n = n;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

//    public String getQuality() {
//        return quality;
//    }
//
//    public void setQuality(String quality) {
//        this.quality = quality;
//    }
//
//    public String getResponse_format() {
//        return response_format;
//    }
//
//    public void setResponse_format(String response_format) {
//        this.response_format = response_format;
//    }
//
//
//    public String getStyle() {
//        return style;
//    }
//
//    public void setStyle(String style) {
//        this.style = style;
//    }
}
