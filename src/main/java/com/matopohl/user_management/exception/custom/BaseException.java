package com.matopohl.user_management.exception.custom;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
public class BaseException extends Exception {

    private String[] args;

    private HttpStatus status;

    public BaseException(String message, String[] args, HttpStatus status) {
        super(message);
        this.args = args;
        this.status = status;
    }

    public BaseException(String message, String[] args, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.args = args;
        this.status = status;
    }
}
