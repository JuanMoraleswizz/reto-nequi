package com.nequi.franchises.application.exception;

import java.util.UUID;

public class BranchNotFoundException extends RuntimeException {
    public BranchNotFoundException(UUID id) {
        super("Branch with id " + id + " not found");
    }
}
