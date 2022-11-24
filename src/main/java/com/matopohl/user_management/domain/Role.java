package com.matopohl.user_management.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Audited
@Table(name = "roles")
@Entity
public class Role {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "role_id", nullable = false, unique = true, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, columnDefinition = "text")
    private String name;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(name = "roles_roles", joinColumns = {@JoinColumn(name = "fk_role_id", referencedColumnName = "role_id")},
            inverseJoinColumns = {@JoinColumn(name = "fk_parent_role_id", referencedColumnName = "role_id")})
    private List<Role> parentRoles = new ArrayList<>();

    @ManyToMany(mappedBy = "parentRoles", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private List<Role> childrenRoles = new ArrayList<>();

    @ManyToMany(mappedBy = "roles")
    private List<User> users = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(name = "roles_authorities", joinColumns = {@JoinColumn(name = "fk_role_id", referencedColumnName = "role_id")},
            inverseJoinColumns = {@JoinColumn(name = "fk_authority_id", referencedColumnName = "authority_id")})
    private List<Authority> roleAuthorities = new ArrayList<>();

    Stream<Role> flattened() {
        return Stream.concat(
                Stream.of(this),
                childrenRoles.stream().flatMap(Role::flattened)
        );
    }

}
