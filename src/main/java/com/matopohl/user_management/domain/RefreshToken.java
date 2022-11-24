package com.matopohl.user_management.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
@Audited
@Table(name = "refresh_tokens", schema = "public")
@Entity
public class RefreshToken {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "refresh_token_id", nullable = false, unique = true, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "creation_date", nullable = false, updatable = false, columnDefinition = "timestamp with time zone")
    private ZonedDateTime creationDate;

    @Column(name = "expiration_date", updatable = false, columnDefinition = "timestamp with time zone")
    private ZonedDateTime expirationDate;

    @Column(name = "last_refresh_date", columnDefinition = "timestamp with time zone")
    private ZonedDateTime lastRefreshDate;

    @Column(name = "refresh_count", nullable = false, columnDefinition = "bigint")
    Long refreshCount;

    @OneToOne
    @JoinColumn(name = "fk_user_device_id", nullable = false, unique = true, referencedColumnName = "user_device_id", columnDefinition = "uuid")
    private UserDevice userDevice;

}
