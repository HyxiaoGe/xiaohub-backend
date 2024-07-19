package com.xiaohub.exception;

public class SensitiveWordException extends RuntimeException{
    public SensitiveWordException(String errorMessage) {
        super(errorMessage);
    }
}
