package com.matopohl.user_management.model.request;

import com.matopohl.user_management.validator.NotBlankIfPresent;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class ResourcePatchRequest implements Serializable {

    @Size(max = 255)
    @NotBlankIfPresent
    private String requestUrl;

    @Size(max = 8)
    @NotBlankIfPresent
    private String requestMethod;

    @Size(min = 1, message = "{javax.validation.constraints.NotEmpty.message}")
    List<String> authorities;

}
