package com.matopohl.user_management.security;

import com.matopohl.user_management.configuration.constants.ExceptionMessageCode;
import com.matopohl.user_management.domain.LoginAttemptLock;
import com.matopohl.user_management.domain.User;
import com.matopohl.user_management.event.publisher.MyEventPublisher;
import com.matopohl.user_management.exception.custom.LoginException;
import com.matopohl.user_management.repository.LoginAttemptLockCacheRepository;
import com.matopohl.user_management.repository.UserRepository;
import com.matopohl.user_management.service.VerifyUserTokenService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import javax.servlet.http.HttpServletRequest;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Configuration
public class MyAuthenticationProvider implements AuthenticationProvider {

    @Getter
    @Setter
    private UUID loggedUser;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final HttpServletRequest request;
    private final LoginAttemptLockCacheRepository loginAttemptLockCacheRepository;
    private final MyEventPublisher eventPublisher;
    private final VerifyUserTokenService verifyTokenService;

    private static final int MAX_ATTEMPT_COUNT = 10;
    private static final long LOCK_DURATION = 300;

    @SneakyThrows
    @Override
    public Authentication authenticate(Authentication auth) {
        Optional<User> checkUser = userRepository.findByEmail(auth.getName());

        if (checkUser.isPresent()) {
            User user = checkUser.get();

            LoginAttemptLock loginAttemptLock = loginAttemptLockCacheRepository.findByEmail(auth.getName());

            if(loginAttemptLock == null) {
                loginAttemptLock = new LoginAttemptLock().setCount(0).setEmail(user.getEmail()).setUserId(user.getId());
            }

            if(loginAttemptLock.getCount() >= MAX_ATTEMPT_COUNT) {
                throw new LoginException(ExceptionMessageCode.USER_LOCK, new String[]{user.getEmail(), String.valueOf(loginAttemptLockCacheRepository.getRemainingTTL(user.getEmail()))}, HttpStatus.UNAUTHORIZED);
            }

            if (passwordEncoder.matches(auth.getCredentials().toString(), user.getPassword())) {
                MyUserDetails userDetails = new MyUserDetails(user);

                checkUser(userDetails);

                return setAuthentication(userDetails);
            }
            else {
                loginAttemptLock.setCount(loginAttemptLock.getCount() + 1).setTtl(LOCK_DURATION);

                loginAttemptLockCacheRepository.save(loginAttemptLock);

                if(loginAttemptLock.getCount() >= MAX_ATTEMPT_COUNT) {
                    user.setLock(true);

                    userRepository.save(user);

                    throw new LoginException(ExceptionMessageCode.USER_LOCK, new String[]{user.getEmail(), String.valueOf(loginAttemptLockCacheRepository.getRemainingTTL(user.getEmail()))}, HttpStatus.UNAUTHORIZED);
                }
            }
        }

        throw new LoginException(ExceptionMessageCode.USER_BAD_CREDENTIALS, null, HttpStatus.UNAUTHORIZED);
    }

    private void checkUser(MyUserDetails userDetails) throws LoginException {
        User user = userDetails.getUser();

        if(!user.getVerified()) {
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

            if(user.getVerifyUserToken().getExpirationDate().isBefore(now)) {
                verifyTokenService.createVerifyUserToken(user, false);

                User createdUser = userRepository.save(user);

                eventPublisher.publishLoginFailUserNotVerifiedEvent(createdUser, createdUser.getVerifyUserToken());

                throw new LoginException(ExceptionMessageCode.USER_UNVERIFIED_AND_EXPIRED, new String[]{userDetails.getEmail()}, HttpStatus.UNAUTHORIZED);
            }

            throw new LoginException(ExceptionMessageCode.USER_UNVERIFIED, new String[]{userDetails.getEmail()}, HttpStatus.UNAUTHORIZED);
        }

        if(user.getBan()) {
            throw new LoginException(ExceptionMessageCode.USER_BAN, new String[]{userDetails.getEmail()}, HttpStatus.UNAUTHORIZED);
        }
    }

    public void setAuthentication(String id, String[] authorities) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(new MyUserDetails(new User().setId(UUID.fromString(id))), null, Arrays.stream(authorities).map(SimpleGrantedAuthority::new).toList());

        setAuthentication(authenticationToken);
    }

    public void setDefaultAuthentication() {
        setAuthentication(new MyUserDetails(new User()));
    }

    private Authentication setAuthentication(MyUserDetails userDetails) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails,null, userDetails.getAuthorities());

        return setAuthentication(authenticationToken);
    }

    private Authentication setAuthentication(UsernamePasswordAuthenticationToken authenticationToken) {
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        setLoggedUser(((MyUserDetails) authenticationToken.getPrincipal()).getUserId());

        return authenticationToken;
    }

    @Override
    public boolean supports(Class<?> auth) {
        return auth.equals(UsernamePasswordAuthenticationToken.class);
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(new MyAuthenticationProvider(userRepository, passwordEncoder, request, loginAttemptLockCacheRepository, eventPublisher, verifyTokenService))
                .build();
    }

    @Bean
    AuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return new DefaultAuthenticationEventPublisher(applicationEventPublisher);
    }

}