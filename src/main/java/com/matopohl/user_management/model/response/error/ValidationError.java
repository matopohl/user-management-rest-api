package com.matopohl.user_management.model.response.error;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Setter
@Accessors(chain = true)
public class ValidationError extends BaseError implements Serializable {

    private String field;

}
