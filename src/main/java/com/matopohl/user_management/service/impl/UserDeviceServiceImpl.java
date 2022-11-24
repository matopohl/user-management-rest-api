package com.matopohl.user_management.service.impl;

import com.matopohl.user_management.domain.User;
import com.matopohl.user_management.domain.UserDevice;
import com.matopohl.user_management.service.UserDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua_parser.Client;
import ua_parser.Parser;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserDeviceServiceImpl implements UserDeviceService {

    private static final String UNKNOWN = "unknown";

    public String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    public String getDeviceDetails(HttpServletRequest request) {
        return getDeviceDetails(getUserAgent(request));
    }

    public String getDeviceDetails(String userAgent) {
        String deviceDetails = UNKNOWN;

        Parser uaParser = new Parser();

        Client client = uaParser.parse(userAgent);

        if(client != null) {
            deviceDetails = String.format("%s %s.%s, %s %s.%s, %s", client.os.family, client.os.major, client.os.minor, client.userAgent.family, client.userAgent.major, client.userAgent.minor, client.device.family);
        }

        return deviceDetails;
    }

    public String getIp(HttpServletRequest request) {
        String ip;

        String clientXForwardedForIp = request.getHeader("X-Forwarded-For");

        if (clientXForwardedForIp != null) {
            ip = parseXForwardedHeader(clientXForwardedForIp);
        } else {
            ip = request.getRemoteAddr();
        }

        return ip;
    }

    private String parseXForwardedHeader(String header) {
        return header.split(" *, *")[0];
    }

    @Override
    public Optional<UserDevice> verifyUserDevice(HttpServletRequest request, User user) {
        String userAgent = getUserAgent(request);

        return user.getUserDevices().stream().filter(d -> d.getUserAgent().equals(userAgent)).findAny();
    }

    @Override
    public UserDevice createUserDevice(HttpServletRequest request, User user) {
        String userAgent = getUserAgent(request);

        return new UserDevice()
                .setUserAgent(userAgent)
                .setName(getDeviceDetails(userAgent))
                .setUser(user);
    }

}
