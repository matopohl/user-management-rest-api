package com.matopohl.user_management.model.request;

import com.matopohl.user_management.validator.NotBlankIfPresent;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.io.Serializable;

@Getter
@Setter
public class AuthorityPatchRequest implements Serializable {

    @Size(max = 128)
    @NotBlankIfPresent
    private String name;

}
