package com.xiaohub.exception;

public class ConnectionTimeoutException extends Exception {
    public ConnectionTimeoutException(String message) {
        super(message);
    }

    public ConnectionTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
