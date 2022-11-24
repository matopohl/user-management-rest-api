package com.matopohl.user_management.model.request;

import com.matopohl.user_management.validator.File;
import com.matopohl.user_management.validator.NotBlankIfPresent;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class UserPatchRequest implements Serializable {

    @Size(max = 128)
    @NotBlankIfPresent
    private String firstName;

    @Size(max = 128)
    @NotBlankIfPresent
    private String lastName;

    @Size(max = 128)
    @NotBlankIfPresent
    private String username;

    @Email
    @Size(max = 128)
    @NotBlankIfPresent
    private String email;

    @Size(min = 8, max = 128)
    @NotBlankIfPresent
    private String password;

    @File(formatProperty = "${my.file.image.format}", sizeProperty = "${my.file.image.size}")
    private MultipartFile profileImage;

    @Size(min = 1, message = "{javax.validation.constraints.NotEmpty.message}")
    List<String> roles;

    @Size(min = 1, message = "{javax.validation.constraints.NotEmpty.message}")
    List<String> authorities;

}
