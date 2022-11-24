package com.matopohl.user_management.model.request;

import com.matopohl.user_management.validator.NotBlankIfPresent;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class RolePatchRequest implements Serializable {

    @Size(max = 128)
    @NotBlankIfPresent
    private String name;

    @Size(min = 1, message = "{javax.validation.constraints.NotEmpty.message}")
    List<String> parentRoles;

    @Size(min = 1, message = "{javax.validation.constraints.NotEmpty.message}")
    List<String> childrenRoles;

    @Size(min = 1, message = "{javax.validation.constraints.NotEmpty.message}")
    List<String> authorities;

}
