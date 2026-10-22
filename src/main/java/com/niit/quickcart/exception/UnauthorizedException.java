package com.niit.quickcart.exception;

/** Thrown when a request has no valid login token. Mapped to HTTP 401. */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
