package com.matopohl.user_management.model.response;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
public class RoleResponse extends RepresentationModel<RoleResponse> implements Serializable {

    private UUID id;
    private String name;
    private List<AuthorityResponse> authorities;
    private List<RoleRoleResponse> parentRoles;
    private List<RoleRoleResponse> childrenRoles;

}
