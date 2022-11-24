package com.matopohl.user_management.validator.impl;

import com.matopohl.user_management.configuration.constants.ValidationMessageCode;
import com.matopohl.user_management.validator.File;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Objects;

public class FileImpl implements ConstraintValidator<File, MultipartFile> {

    @Autowired
    private Environment env;

    private String[] format;

    private long size;

    private boolean mandatory;

    @Override
    public void initialize(File constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);

        if(!constraintAnnotation.formatProperty().isBlank()) {
            format = env.resolvePlaceholders(constraintAnnotation.formatProperty()).split(",");
        }
        else {
            format = constraintAnnotation.format();
        }

        String s;

        if(!constraintAnnotation.sizeProperty().isBlank()) {
            s = env.resolvePlaceholders(constraintAnnotation.sizeProperty());
        }
        else {
            s = constraintAnnotation.size();
        }

        size = DataSize.parse(s).toBytes();

        mandatory = constraintAnnotation.mandatory();
    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext context) {
        HibernateConstraintValidatorContext hibernateContext = context.unwrap(HibernateConstraintValidatorContext.class);
        hibernateContext.disableDefaultConstraintViolation();
        hibernateContext.addMessageParameter( "format", String.join(",", format));

        if(multipartFile == null || multipartFile.isEmpty()) {
            if(mandatory) {
                hibernateContext.buildConstraintViolationWithTemplate(ValidationMessageCode.FILE_MANDATORY).addConstraintViolation();

                return false;
            }
            else {
                return true;
            }
        }

        hibernateContext.buildConstraintViolationWithTemplate(ValidationMessageCode.FILE).addConstraintViolation();

        return isSupportedContentType(Objects.requireNonNull(multipartFile.getContentType())) && fitsSize(multipartFile.getSize());
    }

    private boolean isSupportedContentType(String contentType) {
        return Arrays.stream(format).map(String::toLowerCase).toList().contains(contentType.toLowerCase());
    }

    private boolean fitsSize(long fileSize) {
        return size >= fileSize;
    }

}
