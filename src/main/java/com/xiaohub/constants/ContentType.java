package com.xiaohub.constants;

public enum ContentType {

    URL_ENCODED("application/x-www-form-urlencoded"),
    JSON("application/json"),
    FORM_DATA("multipart/form-data");

    private final String mimeType;

    ContentType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

}
