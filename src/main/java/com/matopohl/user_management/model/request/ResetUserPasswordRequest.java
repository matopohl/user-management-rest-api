package com.matopohl.user_management.model.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Getter
@Setter
public class ResetUserPasswordRequest implements Serializable {

    @Email
    @Size(max = 128)
    @NotBlank
    private String email;

}
