package com.matopohl.user_management.model.request;

import com.matopohl.user_management.validator.File;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class UserUpdateRequest extends UserBaseRequest implements Serializable {

    @File(formatProperty = "${my.file.image.format}", sizeProperty = "${my.file.image.size}", mandatory = true)
    private MultipartFile profileImage;

    @NotNull
    List<String> roles;

    @NotNull
    List<String> authorities;

}
