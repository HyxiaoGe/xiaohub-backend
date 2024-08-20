package com.xiaohub.datadigger.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Map;

public class PlatformUpdates implements Serializable {
    @JsonProperty("platform")
    private Map<String, UpdateDetails> platform;

    public PlatformUpdates() {}

    public Map<String, UpdateDetails> getPlatform() {
        return platform;
    }

    public void setPlatform(Map<String, UpdateDetails> platform) {
        this.platform = platform;
    }

    public static class UpdateDetails implements Serializable {
        @JsonProperty("lastUpdated")
        private long lastUpdated;

        public UpdateDetails() {}

        public long getLastUpdated() {
            return lastUpdated;
        }

        public void setLastUpdated(long lastUpdated) {
            this.lastUpdated = lastUpdated;
        }
    }
}

