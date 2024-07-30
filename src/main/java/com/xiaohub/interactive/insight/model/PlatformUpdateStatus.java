package com.xiaohub.interactive.insight.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlatformUpdateStatus {

    private static final Map<String, Boolean> updatesMap = new ConcurrentHashMap<>();

    public static void setUpdateStatus(String platform, boolean hasNewData) {
        updatesMap.put(platform, hasNewData);
    }

    public static Map<String, Boolean> getAllStatus() {
        return new ConcurrentHashMap<>(updatesMap);
    }

    public static void resetAllStatuses() {
        updatesMap.keySet().forEach(key -> updatesMap.put(key, false));
    }

}
