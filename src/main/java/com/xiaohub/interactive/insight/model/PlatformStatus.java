package com.xiaohub.interactive.insight.model;

public class PlatformStatus {

    private boolean isUpdated;
    private long timestamp;

    public PlatformStatus(boolean isUpdated) {
        this.isUpdated = isUpdated;
        this.timestamp = System.currentTimeMillis();
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public void setUpdated(boolean updated) {
        isUpdated = updated;
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }
}
