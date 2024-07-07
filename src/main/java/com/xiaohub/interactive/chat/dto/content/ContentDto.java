package com.xiaohub.interactive.chat.dto.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextContentDtoDto.class, name = "text"),
        @JsonSubTypes.Type(value = ImageContentDtoDto.class, name = "image_url")
})
public abstract class ContentDto {
    @JsonProperty("type")
    private String type;

    public ContentDto() {}

    public ContentDto(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
