package com.tripleng.shopappserver.exceptions;

public class PermissionDenyException extends Exception {
    public PermissionDenyException(String message) {
        super(message);
    }
}
