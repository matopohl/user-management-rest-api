package com.matopohl.user_management.event;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
public class RegistrationSuccessEvent {

    private UUID userId;
    private String firstName;
    private String lastName;
    private String userName;
    private String email;
    private UUID verifyUserToken;

}
