package com.matopohl.user_management.service;

import com.matopohl.user_management.domain.User;
import com.matopohl.user_management.domain.VerifyUserToken;

public interface VerifyUserTokenService {

    VerifyUserToken createVerifyUserToken(User user, boolean newUser);

}

