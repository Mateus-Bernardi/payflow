package com.payflow.ms_wallet.exception;

public class UnauthorizedRequestException extends RuntimeException {

    public UnauthorizedRequestException(String message) {
        super(message);
    }
}
