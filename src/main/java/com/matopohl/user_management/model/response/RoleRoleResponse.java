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
public class RoleRoleResponse extends RepresentationModel<RoleRoleResponse> implements Serializable {

    private UUID id;
    private String name;

}
