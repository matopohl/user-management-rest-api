package com.matopohl.user_management.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.*;

@Getter
@Setter
@Accessors(chain = true)
@Audited
@Table(name = "users", schema = "public")
@Entity
public class User {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "user_id", nullable = false, unique = true, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "first_name", nullable = false, columnDefinition = "text")
    private String firstName;

    @Column(name = "last_name", nullable = false, columnDefinition = "text")
    private String lastName;

    @Column(name = "username", unique = true, columnDefinition = "text")
    private String username;

    @Column(name = "email", nullable = false, unique = true, columnDefinition = "text")
    private String email;

    @Column(name = "password", nullable = false, columnDefinition = "text")
    private String password;

    @Column(name = "verified", nullable = false, columnDefinition = "boolean")
    private Boolean verified;

    @Column(name = "active", nullable = false, columnDefinition = "boolean")
    private Boolean active;

    @Column(name = "lock", nullable = false, columnDefinition = "boolean")
    private Boolean lock;

    @Column(name = "ban", nullable = false, columnDefinition = "boolean")
    private Boolean ban;

    @Column(name = "creation_date", nullable = false, updatable = false, columnDefinition = "timestamp with time zone")
    private ZonedDateTime creationDate;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "fk_profile_image", referencedColumnName = "document_id", nullable = false, columnDefinition = "uuid")
    private Document profileImage;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(name = "users_roles",joinColumns = {@JoinColumn(name = "fk_user_id",referencedColumnName = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "fk_role_id", referencedColumnName = "role_id")})
    private List<Role> roles = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(name = "users_authorities", joinColumns = {@JoinColumn(name = "fk_user_id", referencedColumnName = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "fk_authority_id", referencedColumnName = "authority_id")})
    private List<Authority> userAuthorities = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserDevice> userDevices = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private VerifyUserToken verifyUserToken;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private ResetUserPasswordToken resetUserPasswordToken;

    public Set<Role> flatRoles() {
        Set<Role> flatRoles = new HashSet<>();

        roles.forEach(r -> flatRoles.addAll(r.flattened().toList()));

        return flatRoles;
    }

    public Set<Authority> flatAuthorities() {
        Set<Authority> flatAuthorities = new HashSet<>();

        roles.forEach(r -> flatAuthorities.addAll(r.flattened().map(Role::getRoleAuthorities).flatMap(List::stream).toList()));
        flatAuthorities.addAll(userAuthorities.stream().toList());

        return flatAuthorities;
    }

}
