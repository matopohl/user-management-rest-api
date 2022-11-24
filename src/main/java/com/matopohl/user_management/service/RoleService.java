package com.matopohl.user_management.service;

import com.matopohl.user_management.exception.custom.BaseException;
import com.matopohl.user_management.exception.custom.EntityConflictException;
import com.matopohl.user_management.exception.custom.NotFoundException;
import com.matopohl.user_management.model.request.RoleCreateRequest;
import com.matopohl.user_management.model.request.RolePatchRequest;
import com.matopohl.user_management.model.request.RoleUpdateRequest;
import com.matopohl.user_management.model.response.RoleResponse;

import java.util.List;

public interface RoleService {

    List<RoleResponse> getRoles(Integer page, Integer size, String sort);

    RoleResponse getRole(String id) throws NotFoundException;

    RoleResponse createRole(RoleCreateRequest roleCreateRequest) throws EntityConflictException;

    RoleResponse updateRole(RoleUpdateRequest roleUpdateRequest, String id) throws NotFoundException, EntityConflictException;

    RoleResponse patchRole(RolePatchRequest rolePatchRequest, String id) throws NotFoundException, EntityConflictException;

    void deleteRole(String id) throws BaseException;
}
