package com.payflow.ms_wallet.exception;

public class DuplicateTransferException extends RuntimeException {

    public DuplicateTransferException(String message) {
        super(message);
    }
}