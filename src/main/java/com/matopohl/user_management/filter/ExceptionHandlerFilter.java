package com.matopohl.user_management.filter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.matopohl.user_management.exception.custom.BaseFilterException;
import com.matopohl.user_management.service.helper.ResponseService;
import com.matopohl.user_management.model.response.base.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

@Slf4j
@RequiredArgsConstructor
@ResponseBody
@Order(Integer.MIN_VALUE + 3)
@Component
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    private final ResponseService responseHelper;
    private final LocaleResolver localeResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
        RepresentationModel<BaseResponse<String>> baseResponse;

        Locale locale = localeResolver.resolveLocale(request);

        try {
            filterChain.doFilter(request, response);
        } catch (BaseFilterException ex) {
            log.error(ex.getMessage(), ex);

            HttpStatus status = ex.getStatus();

            baseResponse = responseHelper.createRepresentationModel(ex.getMessage(), ex.getArgs(), null, status, false, locale);

            writeError(request, response, baseResponse, status);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);

            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

            baseResponse =  responseHelper.createRepresentationModel(ex.getMessage(), null, status, false);

            writeError(request, response, baseResponse, status);
        }
    }

    private void writeError(HttpServletRequest request, HttpServletResponse response, RepresentationModel<BaseResponse<String>> baseResponse, HttpStatus status) throws IOException {
        String accept = request.getHeader(HttpHeaders.ACCEPT);

        if(accept == null || accept.isBlank() || !accept.equals(MediaType.APPLICATION_XML_VALUE)) {
            accept = MediaType.APPLICATION_JSON_VALUE;
        }

        response.setHeader(HttpHeaders.CONTENT_TYPE, accept);
        response.setStatus(status.value());

        String result;

        if(accept.equals(MediaType.APPLICATION_XML_VALUE)) {
            result = convertObjectToXml(baseResponse);
        }
        else {
            result = convertObjectToJson(baseResponse);
        }

        response.getWriter().print(result);
    }

    private String convertObjectToJson(Object object) throws JsonProcessingException {
        if (object == null) {
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper().addMixIn(Object.class, MixInByPropName.class);

        return objectMapper.writeValueAsString(object);
    }

    private String convertObjectToXml(Object object) throws JsonProcessingException {
        if (object == null) {
            return null;
        }

        XmlMapper xmlMapper = new XmlMapper();

        return xmlMapper.writeValueAsString(object);
    }

    @JsonIgnoreProperties(value = {"links"})
    private static class MixInByPropName {
    }

}

