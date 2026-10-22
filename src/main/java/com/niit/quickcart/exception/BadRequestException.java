package com.niit.quickcart.exception;

/** Thrown when the request body/data is invalid. Mapped to HTTP 400. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
