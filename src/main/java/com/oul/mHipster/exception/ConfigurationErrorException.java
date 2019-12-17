package com.oul.mHipster.exception;

public class ConfigurationErrorException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private String message;

    public ConfigurationErrorException(String infoError) {
        super(infoError);
        message = infoError;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
