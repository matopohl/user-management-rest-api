package com.matopohl.user_management.model.response;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;

@Getter
@Setter
@Accessors(chain = true)
public class UserLoginResponse extends RepresentationModel<UserLoginResponse> implements Serializable {

    private RefreshTokenBaseResponse refreshToken;
    private String accessToken;
    private UserResponse user;

}
