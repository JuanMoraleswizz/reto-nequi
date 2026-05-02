package com.nequi.franchises.application.exception;

import java.util.UUID;

public class FranchiseNotFoundException extends RuntimeException {
    public FranchiseNotFoundException(UUID id) {
        super("Franchise with id " + id + " not found");
    }
}
