package com.nequi.franchises.application.exception;

public class InvalidStockException extends RuntimeException {
    public InvalidStockException(Integer value) {
        super("Stock cannot be negative. Received: " + value);
    }
}
