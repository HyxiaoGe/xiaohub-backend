package com.xiaohub.interactive.model.request;

public class ImageContent extends Content {
    private ImageUrl image_url;

    public ImageContent() {
        super("image_url");
    }

    public ImageContent(ImageUrl image_url) {
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
