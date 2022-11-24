package com.matopohl.user_management.domain.listener;

import com.matopohl.user_management.domain.RevInfo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.hibernate.envers.RevisionListener;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@RequiredArgsConstructor
public class RevInfoListener implements RevisionListener {

    private final HttpServletResponse response;

    @Value("${my.log.token-header-attribute}")
    private String tokenHeaderAttribute;

    @SneakyThrows
    @Override
    public void newRevision(Object o) {
        RevInfo revisionEntity = (RevInfo) o;

        try {
            revisionEntity.setRequestId(UUID.fromString(response.getHeader(tokenHeaderAttribute)));
        }
        catch (Throwable ignored) {

        }
    }

}
