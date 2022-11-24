package com.matopohl.user_management.service;

import com.matopohl.user_management.domain.User;

import javax.servlet.http.HttpServletRequest;

public interface RefreshTokenService {

    void createRefreshToken(HttpServletRequest request, User user, boolean rememberMe);

}

