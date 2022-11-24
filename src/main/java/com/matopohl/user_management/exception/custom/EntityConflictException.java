package com.matopohl.user_management.exception.custom;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class EntityConflictException extends BaseException {

    @Getter
    private final List<Error> errors = new ArrayList<>();

    public void addError(String message, String[] args, String field) {
        errors.add(new Error(message, args, field));
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @AllArgsConstructor
    public static class Error {
        private String message;
        private String[] args;
        private String field;
    }

}
