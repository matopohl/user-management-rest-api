package com.matopohl.user_management.validator.impl;

import com.matopohl.user_management.validator.NotBlankIfPresent;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotBlankIfPresentImpl implements ConstraintValidator<NotBlankIfPresent, String> {

    @Override
    public boolean isValid(String s, ConstraintValidatorContext context) {
        if (s == null) {
            return true;
        }

        return !s.isBlank();
    }

}
