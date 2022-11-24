package com.matopohl.user_management.filter;

import com.matopohl.user_management.domain.Log;
import com.matopohl.user_management.domain.RevInfo;
import com.matopohl.user_management.repository.LogRepository;
import com.matopohl.user_management.repository.RevInfoRepository;
import com.matopohl.user_management.security.MyAuthenticationProvider;
import com.matopohl.user_management.service.UserDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Order(Integer.MIN_VALUE + 2)
@Component
public class LogFilter extends OncePerRequestFilter {

    private final LogRepository logRepository;
    private final RevInfoRepository revInfoRepository;
    private final UserDeviceService userDeviceService;
    private final MyAuthenticationProvider myAuthenticationProvider;

    @Value("${my.log.token-header-attribute}")
    private String tokenHeaderAttribute;

    public static final String REQUEST = "REQUEST";
    private static final String RESPONSE = "RESPONSE";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        boolean multipart = false;

        ContentCachingRequestWrapper requestWrapper = (ContentCachingRequestWrapper) request;
        ContentCachingResponseWrapper responseWrapper = (ContentCachingResponseWrapper) response;

        if (requestWrapper.getContentType() != null && requestWrapper.getContentType().startsWith("multipart/form-data")) {
            multipart = true;
        }

        String requestPayload = getRequestPayload(requestWrapper, multipart);

        filterChain.doFilter(request, response);

        if (!multipart) {
            requestPayload = new String (requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
        }

        String responsePayload = getResponsePayload(responseWrapper).trim();

        StringBuilder url = new StringBuilder();
        url.append(requestWrapper.getRequestURL());
        if(requestWrapper.getQueryString() != null) {
            url.append("?").append(requestWrapper.getQueryString());
        }
        String requestUrl = url.toString();

        logSlf4j(requestUrl, requestWrapper, responseWrapper, requestPayload, responsePayload);

        logDb(requestUrl, requestWrapper, responseWrapper, requestPayload, responsePayload);
    }

    private void logDb(String requestUrl, ContentCachingRequestWrapper requestWrapper, ContentCachingResponseWrapper responseWrapper, String requestPayload, String responsePayload) {
        UUID responseId = UUID.fromString(responseWrapper.getHeader(tokenHeaderAttribute));

        String userAgent = requestWrapper.getHeader("User-Agent");

        Log log = new Log()
                .setDate(ZonedDateTime.now())
                .setUser(myAuthenticationProvider.getLoggedUser())
                .setRequestId(responseId)
                .setRequestUrl(requestUrl)
                .setRequestMethod(requestWrapper.getMethod())
                .setRequestContentType(requestWrapper.getContentType())
                .setRequestRemoteAddress(userDeviceService.getIp(requestWrapper))
                .setRequestUserAgent(userAgent)
                .setRequestDevice(userDeviceService.getDeviceDetails(userAgent))
                .setRequestSessionId(requestWrapper.getRequestedSessionId())
                .setRequest(requestPayload)
                .setResponseStatus(responseWrapper.getStatus())
                .setResponseContentType(responseWrapper.getContentType())
                .setResponse(responsePayload);

        Optional<RevInfo> rev = revInfoRepository.findByRequestId(responseId);

        if(rev.isPresent()) {
            RevInfo revInfo = rev.get();

            revInfo.setLog(log);

            log.setRevInfo(revInfo);

            revInfoRepository.save(revInfo);
        }
        else {
            logRepository.save(log);
        }
    }

    private void logSlf4j(String requestUrl, ContentCachingRequestWrapper requestWrapper, ContentCachingResponseWrapper responseWrapper, String requestPayload, String responsePayload) {
        log.info("{}\n{} {} {} {} {} {}\n{}", REQUEST, requestUrl, requestWrapper.getMethod(), requestWrapper.getContentType(), userDeviceService.getIp(requestWrapper), requestWrapper.getHeader("User-Agent"), requestWrapper.getRequestedSessionId(), requestPayload);

        log.info("{}\n{} {}\n{}", RESPONSE, responseWrapper.getStatus(), responseWrapper.getContentType(), responsePayload);

    }

    @SuppressWarnings("all")
    private String getRequestPayload(ContentCachingRequestWrapper requestWrapper, boolean multipart) throws ServletException, IOException {
        StringBuilder sb = new StringBuilder();

        if(multipart) {
            requestWrapper.getParts().forEach(part -> {
                String name = part.getName();

                sb.append(name).append(" ").append(part.getContentType()).append(":").append("\n");

                if(requestWrapper.getParameter(name) == null) {
                    sb.append(part.getSubmittedFileName()).append(" ").append(part.getSize()).append("\n");
                }
                else {
                    sb.append(requestWrapper.getParameter(name)).append("\n");
                }
            });
        }

        return sb.toString();
    }

    private String getResponsePayload(ContentCachingResponseWrapper responseWrapper) {
        String responsePayload;

        if(MediaType.IMAGE_PNG_VALUE.equals(responseWrapper.getContentType()) || MediaType.IMAGE_JPEG_VALUE.equals(responseWrapper.getContentType())) {
            responsePayload = responseWrapper.getContentSize() + "\n";
        }
        else {
            responsePayload = new String (responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8) + "\n";
        }

        return responsePayload;
    }

}
