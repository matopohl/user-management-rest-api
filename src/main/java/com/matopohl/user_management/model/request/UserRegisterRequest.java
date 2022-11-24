package com.matopohl.user_management.model.request;

import com.matopohl.user_management.validator.File;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Getter
@Setter
public class UserRegisterRequest extends UserBaseRequest implements Serializable {

    @File(formatProperty = "${my.file.image.format}", sizeProperty = "${my.file.image.size}")
    private MultipartFile profileImage;

}
