package com.nequi.franchises.application.exception;

public class FranchiseNameAlreadyExistsException extends RuntimeException {
    public FranchiseNameAlreadyExistsException(String name) {
        super("A franchise with name '" + name + "' already exists");
    }
}
