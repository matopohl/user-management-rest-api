package com.matopohl.user_management.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matopohl.user_management.event.RegistrationSuccessEvent;
import com.matopohl.user_management.event.ResetUserPasswordSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MyEventListener {

    private static final String USER_REGISTRATION_SUCCESS = "user-registration-success";
    private static final String RESET_USER_PASSWORD_SUCCESS = "reset-user-password-success";

    private final KafkaTemplate<String, String> kafkaTemplate;

    @EventListener
    public void handleRegistrationSuccessEvent(RegistrationSuccessEvent event) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        kafkaTemplate.send(USER_REGISTRATION_SUCCESS, objectMapper.writeValueAsString(event));
    }

    @EventListener
    public void handleResetUserPasswordSuccessEvent(ResetUserPasswordSuccessEvent event) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        kafkaTemplate.send(RESET_USER_PASSWORD_SUCCESS, objectMapper.writeValueAsString(event));
    }

    @KafkaListener(topics = USER_REGISTRATION_SUCCESS, groupId = USER_REGISTRATION_SUCCESS)
    public void consumeUserRegistrationSuccess(String message) {
        log.error("####################################################################################################################################################################");
        log.info("for reference only, use verifyToken to verify new user: {}", message);
        log.error("####################################################################################################################################################################");
    }

    @KafkaListener(topics = RESET_USER_PASSWORD_SUCCESS, groupId = RESET_USER_PASSWORD_SUCCESS)
    public void consumeResetUserPasswordSuccess(String message) {
        log.error("####################################################################################################################################################################");
        log.info("for reference only, use resetUserPasswordToken to reset user password: {}", message);
        log.error("####################################################################################################################################################################");
    }

}
