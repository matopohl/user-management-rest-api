package com.matopohl.user_management.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
@Table(name = "log", schema = "audit")
@Entity
public class Log implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "log_id", nullable = false, unique = true, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "date", nullable = false, updatable = false, columnDefinition = "timestamp with time zone")
    private ZonedDateTime date;

    @Column(name = "user_id", updatable = false, columnDefinition = "uuid")
    private UUID user;

    @Column(name = "request_id", unique = true, columnDefinition = "uuid")
    private UUID requestId;

    @Column(name = "request_url", nullable = false, updatable = false, columnDefinition = "text")
    private String requestUrl;

    @Column(name = "request_method", columnDefinition = "text")
    private String requestMethod;

    @Column(name = "request_content_type", columnDefinition = "text")
    private String requestContentType;

    @Column(name = "request_remote_address", columnDefinition = "text")
    private String requestRemoteAddress;

    @Column(name = "request_user_agent", columnDefinition = "text")
    private String requestUserAgent;

    @Column(name = "request_device", columnDefinition = "text")
    private String requestDevice;

    @Column(name = "request_session_id", columnDefinition = "text")
    private String requestSessionId;

    @Column(name = "request", columnDefinition = "text")
    private String request;

    @Column(name = "response_status", columnDefinition = "integer")
    private Integer responseStatus;

    @Column(name = "response_content_type", columnDefinition = "text")
    private String responseContentType;

    @Column(name = "response", columnDefinition = "text")
    private String response;

    @OneToOne
    @JoinColumn(name = "fk_revinfo_id", unique = true, columnDefinition = "integer")
    private RevInfo revInfo;

}
