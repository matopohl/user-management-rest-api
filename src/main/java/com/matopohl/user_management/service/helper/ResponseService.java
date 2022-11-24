package com.matopohl.user_management.service.helper;

import com.matopohl.user_management.model.response.base.BaseResponse;
import com.matopohl.user_management.model.response.base.MultiErrorResponse;
import com.matopohl.user_management.model.response.error.ValidationError;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor
@Component
public class ResponseService {

    private final MessageSource messageSource;

    public BaseResponse<String> createBaseResponse(String messageCode, String[] args, HttpStatus status, boolean success, Locale locale) {
        return createBaseResponse(messageSource.getMessage(messageCode, args, messageCode, locale), status, success);
    }

    public <T> BaseResponse<T> createBaseResponse(T message, HttpStatus status, boolean success) {
        return new BaseResponse<T>()
                .setMessage(message)
                .setSuccess(success)
                .setStatus(status);
    }

    public RepresentationModel<BaseResponse<String>> createRepresentationModel(String messageCode, String[] args, List<Link> links, HttpStatus status, boolean success, Locale locale) {
        BaseResponse<String> baseResponse = createBaseResponse(messageCode, args, status, success, locale);

        return createRepresentationModel(baseResponse, links);
    }

    public <T> RepresentationModel<BaseResponse<T>> createRepresentationModel(T message, List<Link> links, HttpStatus status, boolean success) {
        BaseResponse<T> baseResponse = createBaseResponse(message, status, success);

        return createRepresentationModel(baseResponse, links);
    }

    public <T> RepresentationModel<BaseResponse<T>> createRepresentationModel(BaseResponse<T> baseResponse, List<Link> links) {
        if(links == null) {
            links = new ArrayList<>();
        }

        return (RepresentationModel<BaseResponse<T>>) CollectionModel.of(baseResponse, links);
    }

    public ResponseEntity<RepresentationModel<BaseResponse<String>>> createResponseEntity(String messageCode, String[] args, List<Link> links, HttpStatus status, boolean success, Locale locale) {
        return new ResponseEntity<>(createRepresentationModel(messageCode, args, links, status, success, locale), status);
    }

    public <T> ResponseEntity<RepresentationModel<BaseResponse<T>>> createResponseEntity(T message, List<Link> links, HttpStatus status, boolean success) {
        return new ResponseEntity<>(createRepresentationModel(message, links, status, success), status);
    }

    public RepresentationModel<BaseResponse<String>> createValidationErrorRepresentationModel(String messageCode, String[] args, List<ValidationError> errors, List<Link> links, HttpStatus status, Locale locale) {
        MultiErrorResponse<String, ValidationError> a = (MultiErrorResponse<String, ValidationError>) new MultiErrorResponse<String, ValidationError>()
                .setErrors(errors)
                .setMessage(messageSource.getMessage(messageCode, args, messageCode, locale))
                .setSuccess(false)
                .setStatus(status);

        if(links == null) {
            links = new ArrayList<>();
        }

        return (RepresentationModel<BaseResponse<String>>) CollectionModel.of(a, links);
    }

    public ResponseEntity<RepresentationModel<BaseResponse<String>>> createValidationErrorResponseEntity(String messageCode, String[] args, List<ValidationError> errors, List<Link> links, HttpStatus status, Locale locale) {
        return new ResponseEntity<>(createValidationErrorRepresentationModel(messageCode, args, errors, links, status, locale), status);
    }

}
