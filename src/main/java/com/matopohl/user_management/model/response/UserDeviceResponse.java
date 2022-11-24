package com.matopohl.user_management.model.response;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
public class UserDeviceResponse extends RepresentationModel<UserDeviceResponse> implements Serializable {

    private UUID id;
    private String name;
    private String userAgent;
    private RefreshTokenBaseResponse refreshToken;

}
