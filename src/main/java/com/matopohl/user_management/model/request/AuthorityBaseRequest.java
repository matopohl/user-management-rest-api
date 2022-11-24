package com.matopohl.user_management.model.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Getter
@Setter
public class AuthorityBaseRequest implements Serializable {

    @Size(max = 128)
    @NotBlank
    private String name;

}
