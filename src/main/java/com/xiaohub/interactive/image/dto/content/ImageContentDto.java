package com.xiaohub.interactive.image.dto.content;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ImageContentDto {

    private long created;
    private List<DataItem> data;

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public List<DataItem> getData() {
        return data;
    }

    public void setData(List<DataItem> data) {
        this.data = data;
    }

    public static class DataItem {
        @JsonProperty("revised_prompt")
        private String revisedPrompt;
        private String url;

        public String getRevisedPrompt() {
            return revisedPrompt;
        }

        public void setRevisedPrompt(String revisedPrompt) {
            this.revisedPrompt = revisedPrompt;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
