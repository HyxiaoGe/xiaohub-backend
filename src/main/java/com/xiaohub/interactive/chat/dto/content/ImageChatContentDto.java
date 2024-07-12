package com.xiaohub.interactive.chat.dto.content;

public class ImageChatContentDto extends ChatContentDto {
    private ImageUrl image_url;

    public ImageChatContentDto() {
        super("image_url");
    }

    public ImageChatContentDto(ImageUrl image_url) {
        super("image_url");
        this.image_url = image_url;
    }

    public ImageUrl getImage_url() {
        return image_url;
    }

    public void setImage_url(ImageUrl image_url) {
        this.image_url = image_url;
    }
}
