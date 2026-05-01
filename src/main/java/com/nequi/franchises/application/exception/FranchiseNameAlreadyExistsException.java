package com.nequi.franchises.application.exception;

public class FranchiseNameAlreadyExistsException extends RuntimeException {
    public FranchiseNameAlreadyExistsException(String name) {
        super("Franchise already exists with name: " + name);
    }
}
