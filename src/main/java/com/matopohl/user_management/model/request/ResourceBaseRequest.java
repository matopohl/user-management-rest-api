package com.matopohl.user_management.model.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Getter
@Setter
public class ResourceBaseRequest implements Serializable {

    @Size(max = 255)
    @NotBlank
    private String requestUrl;

    @Size(max = 8)
    @NotBlank
    private String requestMethod;

}
