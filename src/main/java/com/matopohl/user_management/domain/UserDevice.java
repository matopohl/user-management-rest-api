package com.matopohl.user_management.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
@Audited
@Table(name = "user_devices", schema = "public")
@Entity
public class UserDevice {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "user_device_id", nullable = false, unique = true, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name", nullable = false, updatable = false, columnDefinition = "text")
    private String name;

    @Column(name = "user_agent", nullable = false, updatable = false, columnDefinition = "text")
    private String userAgent;

    @ManyToOne
    @JoinColumn(name="fk_user_id", nullable = false, referencedColumnName = "user_id", columnDefinition = "uuid")
    private User user;

    @OneToOne(mappedBy = "userDevice", cascade = CascadeType.ALL, orphanRemoval = true)
    private RefreshToken refreshToken;

}