package com.xiaohub.constants;

public enum Platform {
    KR_36("36kr"),
    CHAPING("chaping"),
    ALIRESEARCH("aliresearch");

    private final String platform;

    Platform(String platform) {
        this.platform = platform;
    }

    public String getPlatform() {
        return platform;
    }
}
