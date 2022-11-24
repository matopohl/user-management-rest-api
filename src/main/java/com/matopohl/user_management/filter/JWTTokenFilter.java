package com.matopohl.user_management.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.matopohl.user_management.security.MyAuthenticationProvider;
import com.matopohl.user_management.service.helper.JWTTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Order(0)
@Component
public class JWTTokenFilter extends OncePerRequestFilter {

    private final JWTTokenService jwtHelper;
    private final MyAuthenticationProvider myAuthenticationProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        DecodedJWT decodedToken = jwtHelper.verifyToken(request, false);

        if(decodedToken == null) {
            myAuthenticationProvider.setDefaultAuthentication();
        }
        else {
            myAuthenticationProvider.setAuthentication(decodedToken.getSubject(), decodedToken.getClaim(JWTTokenService.AUTHORITIES).asArray(String.class));
        }

        filterChain.doFilter(request, response);
    }

}
