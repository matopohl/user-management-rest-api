package com.matopohl.user_management.model.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class RoleCreateRequest extends RoleBaseRequest implements Serializable {

    @NotNull
    List<String> parentRoles;

    @NotNull
    List<String> childrenRoles;

    @NotNull
    List<String> authorities;

}
