package com.matopohl.user_management.service;

import com.matopohl.user_management.exception.custom.BaseException;
import com.matopohl.user_management.exception.custom.EntityConflictException;
import com.matopohl.user_management.exception.custom.NotFoundException;
import com.matopohl.user_management.model.request.UserPatchRequest;
import com.matopohl.user_management.model.request.UserRegisterRequest;
import com.matopohl.user_management.model.request.UserUpdateRequest;
import com.matopohl.user_management.model.response.UserResponse;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface UserService {

    List<UserResponse> getUsers(Integer page, Integer size, String sort);

    UserResponse getUser(String id) throws NotFoundException;

    byte[] getProfileImage(String id) throws IOException, NotFoundException;

    UserResponse registerUser(UserRegisterRequest userRequest, HttpServletRequest request) throws IOException, ExecutionException, InterruptedException, EntityConflictException;

    UserResponse updateUser(UserUpdateRequest userUpdateRequest, String id) throws NotFoundException, EntityConflictException, IOException;

    UserResponse patchUser(UserPatchRequest userPatchRequest, String id) throws EntityConflictException, NotFoundException, IOException;

    void deactivateUser(String id) throws BaseException;

    void unlockUser(String id) throws NotFoundException;

    void banUser(String id) throws NotFoundException;

    void unbanUser(String id) throws NotFoundException;

}
