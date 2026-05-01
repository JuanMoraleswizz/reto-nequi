package com.nequi.franchises.application.exception;

public class FranchiseNotFoundException extends RuntimeException {
    public FranchiseNotFoundException(Long id) {
        super("Franchise not found with id: " + id);
    }
}
