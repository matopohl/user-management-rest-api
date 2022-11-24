package com.matopohl.user_management.model.request;

import com.matopohl.user_management.validator.NotBlankIfPresent;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Getter
@Setter
public class UserBaseRequest implements Serializable {

    @Size(max = 128)
    @NotBlank
    private String firstName;

    @Size(max = 128)
    @NotBlank
    private String lastName;

    @Size(max = 128)
    @NotBlankIfPresent
    private String username;

    @Email
    @Size(max = 128)
    @NotBlank
    private String email;

    @Size(min = 8, max = 128)
    @NotBlank
    private String password;

}
