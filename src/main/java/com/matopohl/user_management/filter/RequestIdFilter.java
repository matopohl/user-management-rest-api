package com.matopohl.user_management.filter;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Order(Integer.MIN_VALUE + 1)
@Component
public class RequestIdFilter extends OncePerRequestFilter {

    @Value("${my.log.token-header-attribute}")
    private String tokenHeaderAttribute;

    @Value("${my.log.token-slf4j-attribute}")
    private String tokenSlf4jAttribute;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String token;

        if (StringUtils.hasText(tokenHeaderAttribute) && StringUtils.hasText(request.getHeader(tokenHeaderAttribute))) {
            token = request.getHeader(tokenHeaderAttribute);
        } else {
            token = UUID.randomUUID().toString();
        }

        if (StringUtils.hasText(tokenSlf4jAttribute)) {
            ThreadContext.clearAll();

            ThreadContext.put(tokenSlf4jAttribute, token);
        }

        response.addHeader(tokenHeaderAttribute, token);

        filterChain.doFilter(request, response);
    }

}
