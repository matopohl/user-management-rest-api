package com.matopohl.user_management.service.impl;

import com.matopohl.user_management.domain.User;
import com.matopohl.user_management.domain.VerifyUserToken;
import com.matopohl.user_management.service.VerifyUserTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@RequiredArgsConstructor
@Service
public class VerifyUserTokenServiceImpl implements VerifyUserTokenService {

    @Value("${my.user.verify-token-expiration:1440}")
    private long verifyUserExpiration;

    @Override
    public VerifyUserToken createVerifyUserToken(User user, boolean newUser) {
        ZonedDateTime expirationDate;

        if(newUser) {
            expirationDate = user.getCreationDate();
        }
        else {
            expirationDate = ZonedDateTime.now();
        }

        VerifyUserToken verifyToken = new VerifyUserToken()
                .setCreationDate(user.getCreationDate())
                .setExpirationDate(expirationDate.plusMinutes(verifyUserExpiration))
                .setUser(user);

        user.setVerifyUserToken(verifyToken);

        return verifyToken;
    }

}
