package com.matopohl.user_management.exception;

import com.matopohl.user_management.configuration.constants.ExceptionMessageCode;
import com.matopohl.user_management.exception.custom.BaseException;
import com.matopohl.user_management.exception.custom.BaseFilterException;
import com.matopohl.user_management.exception.custom.EntityConflictException;
import com.matopohl.user_management.service.helper.ResponseService;
import com.matopohl.user_management.model.response.base.BaseResponse;
import com.matopohl.user_management.model.response.error.ValidationError;
import com.matopohl.user_management.security.MyUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolationException;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@ResponseBody
@ControllerAdvice
public class MyExceptionHandler {

    private final ResponseService responseService;
    private final MessageSource messageSource;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RepresentationModel<BaseResponse<String>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, Locale locale) {
        log.error(ex.getMessage(), ex);

        return responseService.createValidationErrorResponseEntity(ExceptionMessageCode.REQUEST_VALIDATION_EXCEPTION,
                new String[]{String.valueOf(ex.getBindingResult().getFieldErrors().size())},
                ex.getBindingResult().getFieldErrors()
                        .stream()
                        .map(x -> (ValidationError) new ValidationError()
                                .setField(x.getField())
                                .setMessage(x.getDefaultMessage())
                        )
                        .collect(Collectors.toList()),
                null,
                HttpStatus.BAD_REQUEST,
                locale);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RepresentationModel<BaseResponse<String>>> handleConstraintViolationException(ConstraintViolationException ex, Locale locale) {
        log.error(ex.getMessage(), ex);

        return responseService.createValidationErrorResponseEntity(ExceptionMessageCode.REQUEST_VALIDATION_EXCEPTION,
                new String[]{String.valueOf(ex.getConstraintViolations().size())},
                ex.getConstraintViolations()
                        .stream()
                        .map(x -> (ValidationError) new ValidationError()
                                .setField(String.valueOf(x.getPropertyPath()))
                                .setMessage(x.getMessage())
                        )
                        .collect(Collectors.toList()),
                null,
                HttpStatus.BAD_REQUEST,
                locale);
    }

    @ExceptionHandler(EntityConflictException.class)
    public ResponseEntity<RepresentationModel<BaseResponse<String>>> handleEntityConflictException(EntityConflictException ex, Locale locale) {
        log.error(ExceptionMessageCode.REQUEST_VALIDATION_EXCEPTION, ex);

        return responseService.createValidationErrorResponseEntity(ExceptionMessageCode.REQUEST_VALIDATION_EXCEPTION,
                new String[]{String.valueOf(ex.getErrors().size())},
                ex.getErrors()
                        .stream()
                        .map(x -> (ValidationError) new ValidationError()
                                .setField(x.getField())
                                .setMessage(messageSource.getMessage(x.getMessage(), x.getArgs(), x.getMessage(), locale))
                        )
                        .collect(Collectors.toList()),
                null,
                HttpStatus.CONFLICT,
                locale);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<RepresentationModel<BaseResponse<String>>> handleMissingServletRequestPartException(MissingServletRequestPartException ex, Locale locale) {
        log.error(ex.getMessage(), ex);

        return responseService.createResponseEntity(ExceptionMessageCode.REQUEST_MISSING_REQUEST_PART, new String[]{ex.getRequestPartName()}, null, HttpStatus.BAD_REQUEST, false, locale);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RepresentationModel<BaseResponse<String>>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, Locale locale) {
        log.error(ex.getMessage(), ex);

        String value;

        if(ex.getValue() == null) {
            value = "null";
        }
        else {
            value = ex.getValue().toString();
        }

        return responseService.createResponseEntity(ExceptionMessageCode.REQUEST_METHOD_ARGUMENT_TYPE_MISMATCH, new String[]{value, ex.getName()}, null, HttpStatus.BAD_REQUEST, false, locale);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<RepresentationModel<BaseResponse<String>>> handlePropertyReferenceException(PropertyReferenceException ex, Locale locale) {
        log.error(ex.getMessage(), ex);

        return responseService.createResponseEntity(ExceptionMessageCode.REQUEST_PROPERTY_REFERENCE, new String[]{ex.getPropertyName(), null, ex.getType().toString().substring(ex.getType().toString().lastIndexOf(".") + 1).toLowerCase()}, null, HttpStatus.BAD_REQUEST, false, locale);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<RepresentationModel<BaseResponse<String>>> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex, Locale locale) {
        log.error(ex.getMessage(), ex);

        String value;

        if(ex.getContentType() == null) {
            value = "null";
        }
        else {
            value = ex.getContentType().toString();
        }

        return responseService.createResponseEntity(ExceptionMessageCode.REQUEST_HTTP_MEDIA_TYPE_NOT_SUPPORTED, new String[]{value}, null, HttpStatus.BAD_REQUEST, false, locale);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RepresentationModel<BaseResponse<String>>> handleAccessDeniedException(AccessDeniedException ex, Locale locale) {
        log.error(ex.getMessage(), ex);

        if(((MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserId() == null) {
            return responseService.createResponseEntity(ExceptionMessageCode.PAGE_MUST_BE_LOGGED_IN, null, null, HttpStatus.UNAUTHORIZED, false, locale);
        }

        return responseService.createResponseEntity(ExceptionMessageCode.PAGE_ACCESS_DENIED, null, null, HttpStatus.FORBIDDEN, false, locale);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<RepresentationModel<BaseResponse<String>>> handleNoHandlerFoundException(NoHandlerFoundException ex, Locale locale) {
        log.error(ex.getMessage(), ex);

        return responseService.createResponseEntity(ExceptionMessageCode.PAGE_NOT_FOUND, null, null, HttpStatus.NOT_FOUND, false, locale);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<RepresentationModel<BaseResponse<String>>> handleBaseException(BaseException ex, Locale locale) {
        log.error(ex.getMessage(), ex);

        return responseService.createResponseEntity(ex.getMessage(), ex.getArgs(), null, ex.getStatus(), false, locale);
    }

    @ExceptionHandler(BaseFilterException.class)
    public ResponseEntity<RepresentationModel<BaseResponse<String>>> handleBaseFilterException(BaseFilterException ex, Locale locale) {
        log.error(ex.getMessage(), ex);

        return responseService.createResponseEntity(ex.getMessage(), ex.getArgs(), null, ex.getStatus(), false, locale);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<RepresentationModel<BaseResponse<String>>> handleException(Throwable ex) {
         log.error(ex.getMessage(), ex);

        return responseService.createResponseEntity(ex.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR, false);
    }

}
