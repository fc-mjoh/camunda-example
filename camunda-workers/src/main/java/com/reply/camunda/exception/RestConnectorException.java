package com.reply.camunda.exception;

public class RestConnectorException extends RuntimeException {

    public RestConnectorException(String message) {
        super(message);
    }

    public RestConnectorException(String message, Throwable cause) {
        super(message, cause);
    }
}
