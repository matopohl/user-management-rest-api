package com.matopohl.user_management.event.publisher;

import com.matopohl.user_management.domain.ResetUserPasswordToken;
import com.matopohl.user_management.domain.User;
import com.matopohl.user_management.domain.VerifyUserToken;
import com.matopohl.user_management.event.RegistrationSuccessEvent;
import com.matopohl.user_management.event.ResetUserPasswordSuccessEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MyEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishRegistrationSuccessEvent(User user, VerifyUserToken verifyToken) {
        RegistrationSuccessEvent registrationSuccessEvent = new RegistrationSuccessEvent()
                .setUserId(user.getId())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setUserName(user.getUsername())
                .setEmail(user.getEmail())
                .setVerifyUserToken(verifyToken.getId());

        applicationEventPublisher.publishEvent(registrationSuccessEvent);
    }

    public void publishRegistrationFailUserExists(User user) {
        //TODO registration user exists - send email - change password?
    }

    public void publishLoginFailUserNotVerifiedEvent(User user, VerifyUserToken verifyToken) {
        publishRegistrationSuccessEvent(user, verifyToken);
    }

    public void publishLoginFromNewDeviceSuccessEvent(User user) {
        //TODO login from new device - send email - take action?
    }

    public void publishBanEvent(User user) {
        //TODO ban user - send email
    }

    public void publishUnbanEvent(User user) {
        //TODO unban user - send email
    }

    public void publishResetUserPasswordSuccessEvent(User user, ResetUserPasswordToken resetUserPasswordToken) {
        ResetUserPasswordSuccessEvent resetUserPasswordSuccessEvent = new ResetUserPasswordSuccessEvent()
                .setUserId(user.getId())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setUserName(user.getUsername())
                .setEmail(user.getEmail())
                .setResetUserPasswordToken(resetUserPasswordToken.getId());

        applicationEventPublisher.publishEvent(resetUserPasswordSuccessEvent);
    }

    public void publishResetUserPasswordFailEvent(String email) {
        //TODO reset user password - user email dont exists - send email but dont send reset-user-password-token
    }
}
