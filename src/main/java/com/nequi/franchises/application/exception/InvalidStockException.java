package com.nequi.franchises.application.exception;

public class InvalidStockException extends RuntimeException {
    public InvalidStockException(Integer stock) {
        super("Stock value is invalid: " + stock);
    }
}
