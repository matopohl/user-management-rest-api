package com.matopohl.user_management.service;

import com.matopohl.user_management.exception.custom.NotFoundException;
import com.matopohl.user_management.model.request.*;
import com.matopohl.user_management.model.response.RefreshTokenResponse;
import com.matopohl.user_management.model.response.UserLoginResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public interface AuthenticationService {

    UserLoginResponse loginUser(UserLoginRequest userLoginRequest, HttpServletRequest request, HttpServletResponse response) throws Exception;

    RefreshTokenResponse refreshToken(String id, HttpServletRequest request, HttpServletResponse response) throws NotFoundException, IOException, InvalidKeySpecException, NoSuchAlgorithmException;

    void verifyUserToken(String id) throws NotFoundException;

    void resetUserPassword(ResetUserPasswordRequest userResetPasswordRequest, HttpServletRequest request, HttpServletResponse response);

    void resetUserPasswordToken(ResetUserPasswordTokenRequest userResetPasswordTokenRequest, String id) throws NotFoundException;

    void logoutUser(String id, HttpServletRequest request) throws NotFoundException;

}
