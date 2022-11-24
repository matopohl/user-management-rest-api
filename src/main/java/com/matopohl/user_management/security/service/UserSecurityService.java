package com.matopohl.user_management.security.service;

import com.matopohl.user_management.domain.Authority;
import com.matopohl.user_management.domain.Resource;
import com.matopohl.user_management.repository.ResourceRepository;
import com.matopohl.user_management.security.MyUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@SuppressWarnings("unused")
@Service
public class UserSecurityService {

    private final ResourceRepository resourceRepository;

    public boolean isUser(String id){
        UUID userId = ((MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserId();

        if(userId == null || id == null) {
            return false;
        }

        return id.equals(userId.toString());
    }

    public boolean authorize(HttpServletRequest request){
        return authorize(request, null);
    }

    public boolean authorize(HttpServletRequest request, String defaultUrl){
        String url = request.getRequestURI();
        String method = request.getMethod();

        if(url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        if(defaultUrl != null && defaultUrl.endsWith("/")) {
            defaultUrl = defaultUrl.substring(0, defaultUrl.length() - 1);
        }

        List<Resource> foundResources = resourceRepository.findAllByRequestUrlAndRequestMethodOrRequestUrlAndRequestMethod(url, method.toUpperCase(), defaultUrl, method.toUpperCase());

        List<String> userAuthorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        List<String> resourceAuthorities;
        Optional<Resource> resource;
        String finalUrl = url;
        String finalDefaultUrl = defaultUrl;

        resource = foundResources.stream().filter(r -> r.getRequestUrl().equals(finalUrl)).findFirst();

        if(resource.isEmpty() && defaultUrl != null) {
            resource = foundResources.stream().filter(r -> r.getRequestUrl().equals(finalDefaultUrl)).findFirst();
        }

        if(resource.isPresent()) {
            resourceAuthorities = resource.get().getResourceAuthorities().stream().map(Authority::getName).toList();

            return userAuthorities.stream().anyMatch(resourceAuthorities::contains);
        }

        return true;
    }

}
