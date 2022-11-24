package com.matopohl.user_management.mapper;

import com.matopohl.user_management.domain.Role;
import com.matopohl.user_management.model.request.RoleCreateRequest;
import com.matopohl.user_management.model.request.RolePatchRequest;
import com.matopohl.user_management.model.request.RoleUpdateRequest;
import com.matopohl.user_management.model.response.RoleResponse;
import com.matopohl.user_management.model.response.RoleRoleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;

@SuppressWarnings("unused")
@Mapper(componentModel = "spring", uses = {AuthorityMapper.class})
public interface RoleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentRoles", ignore = true)
    @Mapping(target = "childrenRoles", ignore = true)
    @Mapping(target = "roleAuthorities", ignore = true)
    @Mapping(target = "users", ignore = true)
    Role toRole(RoleCreateRequest roleCreateRequest);

    @Mapping(target = "parentRoles", ignore = true)
    @Mapping(target = "childrenRoles", ignore = true)
    @Mapping(target = "roleAuthorities", ignore = true)
    @Mapping(target = "users", ignore = true)
    Role toRole(RoleUpdateRequest roleUpdateRequest, String id);

    default void toRole(RolePatchRequest rolePatchRequest, Role role) {
        if(rolePatchRequest.getName() != null) {
            role.setName(rolePatchRequest.getName());
        }
        if(rolePatchRequest.getParentRoles() != null) {
            role.setParentRoles(new ArrayList<>());
        }
        if(rolePatchRequest.getChildrenRoles() != null) {
            role.setChildrenRoles(new ArrayList<>());
        }
        if(rolePatchRequest.getAuthorities() != null) {
            role.setRoleAuthorities(new ArrayList<>());
        }
    }

    @Mapping(target = "authorities", source = "roleAuthorities")
    RoleResponse toRoleResponse(Role role);

    RoleRoleResponse toRoleRoleResponse(Role role);

}
