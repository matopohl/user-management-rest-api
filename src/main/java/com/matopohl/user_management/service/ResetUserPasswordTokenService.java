package com.matopohl.user_management.service;

import com.matopohl.user_management.domain.User;

public interface ResetUserPasswordTokenService {

    void createResetUserPasswordToken(User user);

}

