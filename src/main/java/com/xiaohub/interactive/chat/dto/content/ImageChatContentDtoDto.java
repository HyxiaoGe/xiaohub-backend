package com.xiaohub.interactive.chat.dto.content;

public class ImageChatContentDtoDto extends ChatContentDto {
    private ImageUrl image_url;

    public ImageChatContentDtoDto() {
        super("image_url");
    }

    public ImageChatContentDtoDto(ImageUrl image_url) {
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
