package com.matopohl.user_management.service.helper;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.internal.Utils;
import com.matopohl.user_management.configuration.constants.ExceptionMessageCode;
import com.matopohl.user_management.domain.JWTTokenBlacklist;
import com.matopohl.user_management.exception.custom.JWTTokenException;
import com.matopohl.user_management.repository.JWTTokenBlacklistCacheRepository;
import com.matopohl.user_management.security.MyUserDetails;
import com.matopohl.user_management.service.UserDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class JWTTokenService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JWTTokenBlacklistCacheRepository jwtTokenBlacklistCacheRepository;
    private final RSAService rsaService;
    private final UserDeviceService userDeviceService;

    @Value("${my.jwt.access-token-expiration:5}")
    private long accessExpiration;

    public static final String AUTHORITIES = "auth";
    private static final String USER_AGENT = "usra";
    private static final String ISSUER = "matopohl";
    private static final String AUDIENCE = "user-management";
    private static final String BEARER = "Bearer ";

    public String generateToken(MyUserDetails userDetails, String userAgent) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        ZonedDateTime issueTime = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime expireTime = ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(accessExpiration);

        objectMapper.registerModule(new JavaTimeModule());

        Algorithm algorithm = Algorithm.RSA256(rsaService.getPublicKey(), rsaService.getPrivateKey());

        return BEARER + JWT.create()
                .withIssuer(ISSUER)
                .withAudience(AUDIENCE)
                .withSubject(userDetails.getUserId().toString())
                .withIssuedAt(issueTime.toInstant())
                .withExpiresAt(expireTime.toInstant())
                .withArrayClaim(AUTHORITIES, userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(String[]::new))
                .withClaim(USER_AGENT, Base64.getEncoder().encodeToString(userAgent.getBytes()))
                .withJWTId(UUID.randomUUID().toString())
                .sign(algorithm);
    }

    public DecodedJWT verifyToken(HttpServletRequest request, boolean mandatory) {
        String token = getToken(request);
        String userAgent = userDeviceService.getUserAgent(request);
        DecodedJWT decodedToken = null;

        if (Utils.isEmpty(token)) {
            if(mandatory) {
                throw new JWTTokenException(ExceptionMessageCode.JW_TOKEN_MISSING, null, HttpStatus.UNAUTHORIZED);
            }
        }
        else {
            if (!token.startsWith(BEARER)) {
                throw new JWTTokenException(ExceptionMessageCode.JWT_TOKEN_UNSUPPORTED, null, HttpStatus.UNAUTHORIZED);
            }

            try {
                decodedToken = verifyToken(token, userAgent);
            } catch (TokenExpiredException ignored) {

            } catch (Throwable ignored) {
                //throw new JWTTokenException(ExceptionMessageCode.BAD_JWT_TOKEN, null, HttpStatus.UNAUTHORIZED, ex);
            }

            if(jwtTokenBlacklistCacheRepository.existsByToken(token)) {
                return null;
            }
        }

        return decodedToken;
    }

    private DecodedJWT verifyToken(String token, String userAgent) throws InvalidKeySpecException, IOException, NoSuchAlgorithmException {
        if (token.startsWith(BEARER)) {
            token = token.substring(token.indexOf(" ") + 1).trim();
        }

        return getJWTVerifier(userAgent).verify(token);
    }

    private JWTVerifier getJWTVerifier(String userAgent) throws InvalidKeySpecException, IOException, NoSuchAlgorithmException {
        Algorithm algorithm = Algorithm.RSA256(rsaService.getPublicKey());

        return JWT.require(algorithm)
                .withIssuer(ISSUER)
                .withAudience(AUDIENCE)
                .withClaim(USER_AGENT, Base64.getEncoder().encodeToString(userAgent.getBytes()))
                .build();
    }

    public String getToken(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.AUTHORIZATION);
    }

    public void blacklistToken(HttpServletRequest request) {
        DecodedJWT decodedToken = verifyToken(request, true);

        if(decodedToken != null) {
            String token = getToken(request);

            JWTTokenBlacklist jwtTokenBlacklisted = new JWTTokenBlacklist()
                    .setToken(token)
                    .setUserId(UUID.fromString(decodedToken.getSubject()))
                    .setTtl(getTTLForToken(decodedToken.getExpiresAt()));

            jwtTokenBlacklistCacheRepository.save(jwtTokenBlacklisted);
        }
    }

    private long getTTLForToken(Date date) {
        long expiration = date.toInstant().getEpochSecond() + 10;
        long now = Instant.now().getEpochSecond();
        return Math.max(0, expiration - now);
    }

}
