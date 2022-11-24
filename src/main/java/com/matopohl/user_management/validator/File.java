package com.matopohl.user_management.validator;

import com.matopohl.user_management.configuration.constants.ValidationMessageCode;
import com.matopohl.user_management.validator.impl.FileImpl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = {FileImpl.class})
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(File.List.class)
public @interface File {

    String message() default ValidationMessageCode.FILE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] format() default {};

    String size() default "10MB";

    boolean mandatory() default false;

    String formatProperty() default "";

    String sizeProperty() default "";

    @Documented
    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
    @Retention(RUNTIME)
    @interface List {
        File[] value();
    }

}
