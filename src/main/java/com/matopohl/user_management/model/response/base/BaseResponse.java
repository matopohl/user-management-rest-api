package com.matopohl.user_management.model.response.base;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

@Accessors(chain = true)
public class BaseResponse<T> extends RepresentationModel<BaseResponse<T>> implements Serializable {

    @Getter
    @Setter
    private boolean success = true;

    @Getter
    private int code;

    @Getter
    private String reason;

    @Getter
    @Setter
    private T message;

    public BaseResponse<T> setStatus(HttpStatus status) {
        this.code = status.value();
        this.reason = status.getReasonPhrase();

        return this;
    }

}
