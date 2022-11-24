package com.matopohl.user_management.exception.custom;

import org.springframework.http.HttpStatus;

public class JWTTokenException extends BaseFilterException {

    public JWTTokenException(String message, String[] args, HttpStatus status) {
        super(message, args, status);
    }

    public JWTTokenException(String message, String[] args, HttpStatus status, Throwable cause) {
        super(message, args, status, cause);
    }

}
