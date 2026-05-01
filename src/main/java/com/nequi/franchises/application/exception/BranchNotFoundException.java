package com.nequi.franchises.application.exception;

public class BranchNotFoundException extends RuntimeException {
    public BranchNotFoundException(Long id) {
        super("Branch not found with id: " + id);
    }
}
