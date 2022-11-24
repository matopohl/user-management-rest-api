package com.matopohl.user_management.model.response;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
public class UserResponse extends RepresentationModel<UserResponse> implements Serializable {

    private UUID id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private Boolean verified;
    private Boolean active;
    private Boolean lock;
    private Boolean ban;
    private ZonedDateTime creationDate;
    private List<RoleResponse> roles;
    private List<AuthorityResponse> authorities;
    private List<RoleRoleResponse> flatRoles;
    private List<AuthorityResponse> flatAuthorities;
    private List<UserDeviceResponse> userDevices;

}
