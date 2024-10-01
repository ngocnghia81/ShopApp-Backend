package com.tripleng.shopappserver.exceptions;

public class ExpiredTokenException extends Throwable {
    public ExpiredTokenException(String tokenExpired) {
        super(tokenExpired);
    }
}
