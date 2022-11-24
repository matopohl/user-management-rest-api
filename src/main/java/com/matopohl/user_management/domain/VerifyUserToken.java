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
@Table(name = "verify_user_tokens", schema = "public")
@Entity
public class VerifyUserToken {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "verify_user_token_id", nullable = false, unique = true, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "creation_date", nullable = false, updatable = false, columnDefinition = "timestamp with time zone")
    private ZonedDateTime creationDate;

    @Column(name = "expiration_date", nullable = false, updatable = false, columnDefinition = "timestamp with time zone")
    private ZonedDateTime expirationDate;

    @Column(name = "verify_date", columnDefinition = "timestamp with time zone")
    private ZonedDateTime verifyDate;

    @OneToOne
    @JoinColumn(name = "fk_user_id", nullable = false, unique = true, referencedColumnName = "user_id", columnDefinition = "uuid")
    private User user;

}
