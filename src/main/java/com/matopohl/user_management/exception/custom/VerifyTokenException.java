package com.matopohl.user_management.exception.custom;

import org.springframework.http.HttpStatus;

public class VerifyTokenException extends BaseFilterException {

    public VerifyTokenException(String message, String[] args, HttpStatus status) {
        super(message, args, status);
    }

    public VerifyTokenException(String message, String[] args, HttpStatus status, Throwable cause) {
        super(message, args, status, cause);
    }

}
