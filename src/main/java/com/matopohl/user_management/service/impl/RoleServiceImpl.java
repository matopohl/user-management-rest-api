package com.matopohl.user_management.service.impl;

import com.matopohl.user_management.configuration.constants.ExceptionMessageCode;
import com.matopohl.user_management.domain.Authority;
import com.matopohl.user_management.domain.Role;
import com.matopohl.user_management.exception.custom.BaseException;
import com.matopohl.user_management.exception.custom.EntityConflictException;
import com.matopohl.user_management.exception.custom.NotFoundException;
import com.matopohl.user_management.mapper.RoleMapper;
import com.matopohl.user_management.model.request.RoleCreateRequest;
import com.matopohl.user_management.model.request.RolePatchRequest;
import com.matopohl.user_management.model.request.RoleUpdateRequest;
import com.matopohl.user_management.model.response.RoleResponse;
import com.matopohl.user_management.repository.AuthorityRepository;
import com.matopohl.user_management.repository.RoleRepository;
import com.matopohl.user_management.service.RoleService;
import com.matopohl.user_management.service.helper.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RequestService requestService;
    private final RoleRepository roleRepository;
    private final AuthorityRepository authorityRepository;

    private static final String REQUEST_FIELD_NAME = "name";
    private static final String REQUEST_FIELD_PARENT_ROLES = "parentRoles#";
    private static final String REQUEST_FIELD_CHILDREN_ROLES = "childrenRoles#";
    private static final String REQUEST_FIELD_AUTHORITIES = "authorities#";

    @Override
    public List<RoleResponse> getRoles(Integer page, Integer size, String sort) {
        Pageable pageable = requestService.getPageable(page, size, sort);

        Page<Role> roles = roleRepository.findAll(pageable);

        return roles.stream().map(roleMapper::toRoleResponse).collect(Collectors.toList());
    }

    @Override
    public RoleResponse getRole(String id) throws NotFoundException {
        UUID uuid = getRoleUUID(id);

        Optional<Role> checkRole = roleRepository.findById(uuid);

        if (checkRole.isPresent()) {
            return roleMapper.toRoleResponse(checkRole.get());
        }

        throw new NotFoundException(ExceptionMessageCode.ROLE_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND);
    }

    @Override
    public RoleResponse createRole(RoleCreateRequest roleCreateRequest) throws EntityConflictException {
        EntityConflictException entityConflictException = new EntityConflictException();

        Optional<Role> checkRole = roleRepository.findByName(roleCreateRequest.getName());

        checkRole(checkRole, entityConflictException);

        Role role = roleMapper.toRole(roleCreateRequest);

        setParentRoles(role, roleCreateRequest.getParentRoles(), entityConflictException);
        setChildrenRoles(role, roleCreateRequest.getChildrenRoles(), entityConflictException);
        setAuthorities(role, roleCreateRequest.getAuthorities(), entityConflictException);

        if(entityConflictException.getErrors().size() > 0) {
            throw entityConflictException;
        }

        Role savedRole = roleRepository.save(role);

        return roleMapper.toRoleResponse(savedRole);
    }

    @Override
    public RoleResponse updateRole(RoleUpdateRequest roleUpdateRequest, String id) throws NotFoundException, EntityConflictException {
        UUID uuid = getRoleUUID(id);

        Optional<Role> checkRole = roleRepository.findById(uuid);

        if (checkRole.isPresent()) {
            EntityConflictException entityConflictException = new EntityConflictException();

            Optional<Role> checkRoleByName = roleRepository.findByNameAndIdNot(roleUpdateRequest.getName(), uuid);

            checkRole(checkRoleByName, entityConflictException);

            Role role = roleMapper.toRole(roleUpdateRequest, id);

            setParentRoles(role, roleUpdateRequest.getParentRoles(), entityConflictException);
            setChildrenRoles(role, roleUpdateRequest.getChildrenRoles(), entityConflictException);
            setAuthorities(role, roleUpdateRequest.getAuthorities(), entityConflictException);

            if(entityConflictException.getErrors().size() > 0) {
                throw entityConflictException;
            }

            Role savedRole = roleRepository.save(role);

            return roleMapper.toRoleResponse(savedRole);
        }

        throw new NotFoundException(ExceptionMessageCode.ROLE_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND);
    }

    @Override
    public RoleResponse patchRole(RolePatchRequest rolePatchRequest, String id) throws NotFoundException, EntityConflictException {
        UUID uuid = getRoleUUID(id);

        Optional<Role> checkRole = roleRepository.findById(uuid);

        if (checkRole.isPresent()) {
            EntityConflictException entityConflictException = new EntityConflictException();

            Optional<Role> checkRoleByName = roleRepository.findByNameAndIdNot(rolePatchRequest.getName(), uuid);

            checkRole(checkRoleByName, entityConflictException);

            Role role = checkRole.get();

            roleMapper.toRole(rolePatchRequest, role);

            setParentRoles(role, rolePatchRequest.getParentRoles(), entityConflictException);
            setChildrenRoles(role, rolePatchRequest.getChildrenRoles(), entityConflictException);
            setAuthorities(role, rolePatchRequest.getAuthorities(), entityConflictException);

            if(entityConflictException.getErrors().size() > 0) {
                throw entityConflictException;
            }

            Role savedRole = roleRepository.save(role);

            return roleMapper.toRoleResponse(savedRole);
        }

        throw new NotFoundException(ExceptionMessageCode.ROLE_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND);
    }

    @Override
    public void deleteRole(String id) throws BaseException {
        UUID uuid = getRoleUUID(id);

        Optional<Role> checkRole = roleRepository.findById(uuid);

        if (checkRole.isPresent()) {
            try {
                checkRole.ifPresent(role -> roleRepository.deleteById(role.getId()));
            }
            catch(DataIntegrityViolationException ex) {
                throw new BaseException(ExceptionMessageCode.ROLE_CANNOT_DELETE, new String[]{id}, HttpStatus.CONFLICT, ex);
            }
            return;
        }

        throw new NotFoundException(ExceptionMessageCode.ROLE_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND);
    }

    private UUID getRoleUUID(String id) throws NotFoundException {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new NotFoundException(ExceptionMessageCode.ROLE_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND, ex);
        }
    }

    private void checkRole(Optional<Role> checkRole, EntityConflictException ex) {
        checkRole.ifPresent(role -> ex.addError(ExceptionMessageCode.ROLE_ALREADY_EXISTS_BY_NAME, new String[]{role.getName()}, REQUEST_FIELD_NAME));
    }

    private void setParentRoles(Role role, List<String> parentRoles, EntityConflictException entityConflictException) {
        if(parentRoles != null) {
            for(int i = 0; i < parentRoles.size(); i++) {
                String roleId = parentRoles.get(i);

                UUID roleUuid;

                try {
                    roleUuid = UUID.fromString(roleId);
                } catch (IllegalArgumentException ex) {
                    entityConflictException.addError(ExceptionMessageCode.ROLE_NOT_FOUND, new String[]{roleId}, REQUEST_FIELD_PARENT_ROLES + i);

                    continue;
                }

                Optional<Role> parentRole = roleRepository.findById(roleUuid);

                if (parentRole.isPresent()) {
                    role.getParentRoles().add(parentRole.get());
                    parentRole.get().getChildrenRoles().add(role);
                } else {
                    entityConflictException.addError(ExceptionMessageCode.ROLE_NOT_FOUND, new String[]{roleId}, REQUEST_FIELD_PARENT_ROLES + i);
                }
            }
        }
    }

    private void setChildrenRoles(Role role, List<String> childrenRoles, EntityConflictException entityConflictException) {
        if(childrenRoles != null) {
            for(int i = 0; i < childrenRoles.size(); i++) {
                String roleId = childrenRoles.get(i);

                UUID roleUuid;

                try {
                    roleUuid = UUID.fromString(roleId);
                } catch (IllegalArgumentException ex) {
                    entityConflictException.addError(ExceptionMessageCode.ROLE_NOT_FOUND, new String[]{roleId}, REQUEST_FIELD_CHILDREN_ROLES + i);

                    continue;
                }

                Optional<Role> childrenRole = roleRepository.findById(roleUuid);

                if (childrenRole.isPresent()) {
                    role.getChildrenRoles().add(childrenRole.get());
                    childrenRole.get().getParentRoles().add(role);
                } else {
                    entityConflictException.addError(ExceptionMessageCode.ROLE_NOT_FOUND, new String[]{roleId}, REQUEST_FIELD_CHILDREN_ROLES + i);
                }
            }
        }
    }

    private void setAuthorities(Role role, List<String> authorities, EntityConflictException entityConflictException) {
        if(authorities != null) {
            for(int i = 0; i < authorities.size(); i++) {
                String authorityId = authorities.get(i);

                UUID authorityUuid;

                try {
                    authorityUuid = UUID.fromString(authorityId);
                } catch (IllegalArgumentException ex) {
                    entityConflictException.addError(ExceptionMessageCode.AUTHORITY_NOT_FOUND, new String[]{authorityId}, REQUEST_FIELD_AUTHORITIES + i);

                    continue;
                }

                Optional<Authority> authority = authorityRepository.findById(authorityUuid);

                if (authority.isPresent()) {
                    role.getRoleAuthorities().add(authority.get());
                } else {
                    entityConflictException.addError(ExceptionMessageCode.AUTHORITY_NOT_FOUND, new String[]{authorityId}, REQUEST_FIELD_AUTHORITIES + i);
                }
            }
        }
    }

}
