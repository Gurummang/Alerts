package com.GASB.alerts.exception;

public class AlertSettingsNotFoundException extends RuntimeException {

    public AlertSettingsNotFoundException(String message) {
        super(message);
    }

    public AlertSettingsNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
