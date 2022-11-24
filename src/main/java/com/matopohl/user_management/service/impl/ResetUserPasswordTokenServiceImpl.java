package com.matopohl.user_management.service.impl;

import com.matopohl.user_management.domain.ResetUserPasswordToken;
import com.matopohl.user_management.domain.User;
import com.matopohl.user_management.service.ResetUserPasswordTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@RequiredArgsConstructor
@Service
public class ResetUserPasswordTokenServiceImpl implements ResetUserPasswordTokenService {

    @Value("${my.user.reset-password-token-expiration:1440}")
    private long resetPasswordExpiration;

    @Override
    public void createResetUserPasswordToken(User user) {
        ResetUserPasswordToken resetUserPasswordToken = new ResetUserPasswordToken()
                .setCreationDate(ZonedDateTime.now(ZoneOffset.UTC))
                .setExpirationDate(ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(resetPasswordExpiration))
                .setUser(user);

        user.setResetUserPasswordToken(resetUserPasswordToken);
    }
}
