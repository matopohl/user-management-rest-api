package com.matopohl.user_management.model.response.base;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class MultiErrorResponse<T, U> extends BaseResponse<T> implements Serializable {

    @JacksonXmlElementWrapper(localName = "errors")
    @JacksonXmlProperty(localName = "error")
    private List<U> errors;

}
