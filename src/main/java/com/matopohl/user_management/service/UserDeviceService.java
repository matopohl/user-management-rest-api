package com.matopohl.user_management.service;

import com.matopohl.user_management.domain.User;
import com.matopohl.user_management.domain.UserDevice;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public interface UserDeviceService {

    String getUserAgent(HttpServletRequest request);

    String getDeviceDetails(HttpServletRequest request);

    String getDeviceDetails(String userAgent);

    String getIp(HttpServletRequest request);

    Optional<UserDevice> verifyUserDevice(HttpServletRequest request, User user);

    UserDevice createUserDevice(HttpServletRequest request, User user);

}
