package com.matopohl.user_management.service.impl;

import com.matopohl.user_management.configuration.constants.ExceptionMessageCode;
import com.matopohl.user_management.domain.*;
import com.matopohl.user_management.event.publisher.MyEventPublisher;
import com.matopohl.user_management.exception.custom.JWTTokenException;
import com.matopohl.user_management.exception.custom.NotFoundException;
import com.matopohl.user_management.exception.custom.VerifyTokenException;
import com.matopohl.user_management.mapper.RefreshTokenMapper;
import com.matopohl.user_management.mapper.UserMapper;
import com.matopohl.user_management.model.request.*;
import com.matopohl.user_management.model.response.RefreshTokenResponse;
import com.matopohl.user_management.model.response.UserLoginResponse;
import com.matopohl.user_management.repository.RefreshTokenRepository;
import com.matopohl.user_management.repository.ResetUserPasswordTokenRepository;
import com.matopohl.user_management.repository.UserRepository;
import com.matopohl.user_management.repository.VerifyUserTokenRepository;
import com.matopohl.user_management.security.MyUserDetails;
import com.matopohl.user_management.service.AuthenticationService;
import com.matopohl.user_management.service.RefreshTokenService;
import com.matopohl.user_management.service.ResetUserPasswordTokenService;
import com.matopohl.user_management.service.UserDeviceService;
import com.matopohl.user_management.service.helper.JWTTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerifyUserTokenRepository verifyTokenRepository;
    private final ResetUserPasswordTokenRepository resetUserPasswordTokenRepository;
    private final ResetUserPasswordTokenService resetUserPasswordTokenService;
    private final AuthenticationManager authenticationManager;
    private final JWTTokenService jwtTokenService;
    private final UserDeviceService userDeviceService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenMapper refreshTokenMapper;
    private final PasswordEncoder passwordEncoder;
    private final MyEventPublisher eventPublisher;

    @Override
    public UserLoginResponse loginUser(UserLoginRequest userLoginRequest, HttpServletRequest request, HttpServletResponse response) throws Exception {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userLoginRequest.getEmail(), userLoginRequest.getPassword());

        Authentication authentication = authenticationManager.authenticate(token);

        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();

        String jwtToken = jwtTokenService.generateToken(userDetails, userDeviceService.getUserAgent(request));

        User user = userDetails.getUser();

        refreshTokenService.createRefreshToken(request, user, userLoginRequest.getRememberMe());

        user.setActive(true);
        user.setLock(false);

        userRepository.save(user);

        UserDevice userDevice = userDeviceService.verifyUserDevice(request, user).orElseThrow(() -> new NotFoundException(ExceptionMessageCode.USER_DEVICE_NOT_FOUND, new String[]{userDeviceService.getUserAgent(request)}, HttpStatus.NOT_FOUND));

        response.setHeader(HttpHeaders.AUTHORIZATION, jwtToken);

        return userMapper.toUserLoginResponse(userDetails.getUser(), userDevice.getRefreshToken(), jwtToken);
    }

    @Override
    public RefreshTokenResponse refreshToken(String id, HttpServletRequest request, HttpServletResponse response) throws NotFoundException, InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        UUID uuid = getRefreshTokenUUID(id);

        Optional<RefreshToken> foundRefreshToken = refreshTokenRepository.findById(uuid);

        if (foundRefreshToken.isPresent()) {
            RefreshToken refreshToken = foundRefreshToken.get();

            if(!refreshToken.getUserDevice().getUserAgent().equals(userDeviceService.getUserAgent(request))) {
                throw new NotFoundException(ExceptionMessageCode.REFRESH_TOKEN_NOT_FOUND, new String[]{uuid.toString()}, HttpStatus.NOT_FOUND);
            }

            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

            if(refreshToken.getExpirationDate() == null || refreshToken.getExpirationDate().isAfter(now)) {
                MyUserDetails userDetails = new MyUserDetails(refreshToken.getUserDevice().getUser());

                String jwtToken = jwtTokenService.generateToken(userDetails, userDeviceService.getUserAgent(request));

                jwtTokenService.blacklistToken(request);

                refreshToken.setRefreshCount(refreshToken.getRefreshCount() + 1).setLastRefreshDate(now);

                refreshTokenRepository.save(refreshToken);

                response.setHeader(HttpHeaders.AUTHORIZATION, jwtToken);

                return refreshTokenMapper.toRefreshTokenResponse(refreshToken, jwtToken);
            }
            else {
                throw new JWTTokenException(ExceptionMessageCode.REFRESH_TOKEN_EXPIRED, null, HttpStatus.UNAUTHORIZED);
            }
        }

        throw new NotFoundException(ExceptionMessageCode.REFRESH_TOKEN_NOT_FOUND, new String[]{uuid.toString()}, HttpStatus.NOT_FOUND);
    }

    @Override
    public void verifyUserToken(String id) throws NotFoundException {
        UUID uuid;

        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new NotFoundException(ExceptionMessageCode.VERIFY_USER_TOKEN_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND, ex);
        }

        Optional<VerifyUserToken> foundVerifyUserToken = verifyTokenRepository.findById(uuid);

        if (foundVerifyUserToken.isPresent()) {
            VerifyUserToken verifyToken = foundVerifyUserToken.get();

            if(verifyToken.getVerifyDate() != null) {
                return;
            }

            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

            if(verifyToken.getExpirationDate().isAfter(now)) {
                verifyToken.setVerifyDate(ZonedDateTime.now(ZoneOffset.UTC));

                verifyToken.getUser().setVerified(true);
                verifyToken.getUser().setActive(true);

                verifyTokenRepository.save(verifyToken);

                return;
            }
            else {
                throw new VerifyTokenException(ExceptionMessageCode.VERIFY_USER_TOKEN_EXPIRED, null, HttpStatus.NOT_FOUND);
            }
        }

        throw new NotFoundException(ExceptionMessageCode.VERIFY_USER_TOKEN_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND);
    }

    @Override
    public void resetUserPassword(ResetUserPasswordRequest userResetPasswordRequest, HttpServletRequest request, HttpServletResponse response) {
        Optional<User> checkUser = userRepository.findByEmail(userResetPasswordRequest.getEmail());

        if (checkUser.isPresent()) {
            User user = checkUser.get();

            resetUserPasswordTokenService.createResetUserPasswordToken(user);

            User savedUser = userRepository.save(user);

            eventPublisher.publishResetUserPasswordSuccessEvent(user, savedUser.getResetUserPasswordToken());

            return;
        }

        eventPublisher.publishResetUserPasswordFailEvent(userResetPasswordRequest.getEmail());

    }

    @Override
    public void resetUserPasswordToken(ResetUserPasswordTokenRequest resetPasswordTokenRequest, String id) throws NotFoundException {
        UUID uuid;

        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new NotFoundException(ExceptionMessageCode.RESET_USER_PASSWORD_TOKEN_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND, ex);
        }

        Optional<ResetUserPasswordToken> foundResetUserPasswordToken = resetUserPasswordTokenRepository.findById(uuid);

        if (foundResetUserPasswordToken.isPresent()) {
            ResetUserPasswordToken resetUserPasswordToken = foundResetUserPasswordToken.get();

            if(resetUserPasswordToken.getVerifyDate() != null) {
                throw new NotFoundException(ExceptionMessageCode.RESET_USER_PASSWORD_TOKEN_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND);
            }

            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

            if(resetUserPasswordToken.getExpirationDate().isAfter(now)) {
                resetUserPasswordToken.setVerifyDate(ZonedDateTime.now(ZoneOffset.UTC));

                resetUserPasswordToken.getUser().setPassword(passwordEncoder.encode(resetPasswordTokenRequest.getPassword()));

                resetUserPasswordTokenRepository.save(resetUserPasswordToken);

                return;
            }
            else {
                throw new VerifyTokenException(ExceptionMessageCode.RESET_USER_PASSWORD_TOKEN_EXPIRED, null, HttpStatus.NOT_FOUND);
            }
        }

        throw new NotFoundException(ExceptionMessageCode.RESET_USER_PASSWORD_TOKEN_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND);
    }

    @Override
    public void logoutUser(String id, HttpServletRequest request) throws NotFoundException {
        UUID uuid = getRefreshTokenUUID(id);

        Optional<RefreshToken> checkRefreshToken = refreshTokenRepository.findById(uuid);

        if(checkRefreshToken.isPresent()) {
            refreshTokenRepository.deleteById(uuid);

            jwtTokenService.blacklistToken(request);

            HttpSession session = request.getSession(false);

            if(session != null) {
                session.invalidate();
            }

            if(request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    cookie.setMaxAge(0);
                }
            }

            return;
        }

        throw new NotFoundException(ExceptionMessageCode.REFRESH_TOKEN_NOT_FOUND, new String[]{uuid.toString()}, HttpStatus.NOT_FOUND);
    }

    private UUID getRefreshTokenUUID(String id) throws NotFoundException {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new NotFoundException(ExceptionMessageCode.REFRESH_TOKEN_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND, ex);
        }
    }

}
