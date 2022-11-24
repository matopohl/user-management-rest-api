package com.matopohl.user_management.mapper;

import com.matopohl.user_management.domain.Document;
import com.matopohl.user_management.domain.RefreshToken;
import com.matopohl.user_management.domain.User;
import com.matopohl.user_management.model.request.UserPatchRequest;
import com.matopohl.user_management.model.request.UserRegisterRequest;
import com.matopohl.user_management.model.request.UserUpdateRequest;
import com.matopohl.user_management.model.response.AuthorityResponse;
import com.matopohl.user_management.model.response.RoleRoleResponse;
import com.matopohl.user_management.model.response.UserLoginResponse;
import com.matopohl.user_management.model.response.UserResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@Mapper(componentModel = "spring", uses = {RoleMapper.class, AuthorityMapper.class, RefreshTokenMapper.class})
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "lock", ignore = true)
    @Mapping(target = "ban", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "profileImage", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "userAuthorities", ignore = true)
    @Mapping(target = "userDevices", ignore = true)
    @Mapping(target = "verifyUserToken", ignore = true)
    @Mapping(target = "resetUserPasswordToken", ignore = true)
    User toUser(UserRegisterRequest userRegisterRequest, PasswordEncoder passwordEncoder);

    @AfterMapping
    default void toUserAfterMapping(UserRegisterRequest userRegisterRequest, PasswordEncoder passwordEncoder, @MappingTarget User user) {
        user.setPassword(passwordEncoder.encode(userRegisterRequest.getPassword()))
                .setVerified(false)
                .setActive(false)
                .setLock(false)
                .setBan(false)
                .setCreationDate(ZonedDateTime.now(ZoneOffset.UTC));
    }

    @Mapping(target = "firstName", source = "userUpdateRequest.firstName")
    @Mapping(target = "lastName", source = "userUpdateRequest.lastName")
    @Mapping(target = "username", source = "userUpdateRequest.username")
    @Mapping(target = "email", source = "userUpdateRequest.email")
    @Mapping(target = "verified", source = "user.verified")
    @Mapping(target = "active", source = "user.active")
    @Mapping(target = "lock", source = "user.lock")
    @Mapping(target = "ban", source = "user.ban")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "creationDate", source = "user.creationDate")
    @Mapping(target = "profileImage", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "userAuthorities", ignore = true)
    @Mapping(target = "userDevices", ignore = true)
    @Mapping(target = "verifyUserToken", ignore = true)
    @Mapping(target = "resetUserPasswordToken", ignore = true)
    User toUser(UserUpdateRequest userUpdateRequest, User user, PasswordEncoder passwordEncoder, String id);

    @AfterMapping
    default void toUserAfterMapping(UserUpdateRequest userUpdateRequest, User user, PasswordEncoder passwordEncoder, String id, @MappingTarget User targetUser) {
        targetUser.setPassword(passwordEncoder.encode(userUpdateRequest.getPassword()));

        if(userUpdateRequest.getProfileImage() != null && !userUpdateRequest.getProfileImage().isEmpty()) {
            try {
                user.setProfileImage(new Document().setContent(userUpdateRequest.getProfileImage().getBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    default void toUser(UserPatchRequest userPatchRequest, User user, PasswordEncoder passwordEncoder) {
        if(userPatchRequest.getFirstName() != null) {
            user.setFirstName(userPatchRequest.getFirstName());
        }
        if(userPatchRequest.getLastName() != null) {
            user.setLastName(userPatchRequest.getLastName());
        }
        if(userPatchRequest.getUsername() != null) {
            user.setUsername(userPatchRequest.getUsername());
        }
        if(userPatchRequest.getEmail() != null) {
            user.setEmail(userPatchRequest.getEmail());
        }
        if(userPatchRequest.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userPatchRequest.getPassword()));
        }
        if(userPatchRequest.getProfileImage() != null) {
            try {
                user.setProfileImage(new Document().setContent(userPatchRequest.getProfileImage().getBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Mapping(target = "flatRoles", ignore = true)
    @Mapping(target = "flatAuthorities", ignore = true)
    @Mapping(target = "authorities", source = "userAuthorities")
    UserResponse toUserResponse(User user);

    @AfterMapping
    default void toUserResponseAfterMapping(User user, @MappingTarget UserResponse userResponse) {
        RoleMapper roleMapper = new RoleMapperImpl();
        AuthorityMapper authorityMapper = new AuthorityMapperImpl();

        List<RoleRoleResponse> flatRoles = new ArrayList<>(user.flatRoles().stream().map(roleMapper::toRoleRoleResponse).toList());
        List<AuthorityResponse> flatAuthorities = new ArrayList<>(user.flatAuthorities().stream().map(authorityMapper::toAuthorityResponse).toList());

        userResponse.setFlatRoles(flatRoles);
        userResponse.setFlatAuthorities(flatAuthorities);
    }

    UserLoginResponse toUserLoginResponse(User user, RefreshToken refreshToken, String accessToken);

}
