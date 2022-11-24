package com.matopohl.user_management.exception.custom;

import org.springframework.http.HttpStatus;

public class LoginException extends BaseException {

    public LoginException(String message, String[] args, HttpStatus status) {
        super(message, args, status);
    }

}
