package com.matopohl.user_management.service.impl;

import com.matopohl.user_management.domain.RefreshToken;
import com.matopohl.user_management.domain.User;
import com.matopohl.user_management.domain.UserDevice;
import com.matopohl.user_management.event.publisher.MyEventPublisher;
import com.matopohl.user_management.service.RefreshTokenService;
import com.matopohl.user_management.service.UserDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final UserDeviceService userDeviceService;
    private final MyEventPublisher eventPublisher;

    @Value("${my.jwt.refresh-token-expiration:1440}")
    private long refreshExpiration;

    @Override
    public void createRefreshToken(HttpServletRequest request, User user, boolean rememberMe) {
        Optional<UserDevice> foundUserDevice = userDeviceService.verifyUserDevice(request, user);

        UserDevice userDevice;

        if(foundUserDevice.isPresent()) {
            userDevice = foundUserDevice.get();
        }
        else {
            userDevice = userDeviceService.createUserDevice(request, user);

            user.getUserDevices().add(userDevice);

            eventPublisher.publishLoginFromNewDeviceSuccessEvent(user);
        }

        RefreshToken refreshToken = new RefreshToken()
                .setCreationDate(ZonedDateTime.now(ZoneOffset.UTC))
                .setExpirationDate(rememberMe ? null : ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(refreshExpiration))
                .setRefreshCount(0L)
                .setUserDevice(userDevice);

        userDevice.setRefreshToken(refreshToken);
    }

}
