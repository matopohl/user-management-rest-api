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

@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Audited
@Table(name = "authorities")
@Entity
public class Authority {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "authority_id", nullable = false, unique = true, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, columnDefinition = "text")
    private String name;

    @ManyToMany(mappedBy = "roleAuthorities")
    private List<Role> roles = new ArrayList<>();

    @ManyToMany(mappedBy = "userAuthorities")
    private List<User> users = new ArrayList<>();

    @ManyToMany(mappedBy = "resourceAuthorities")
    private List<Resource> resources = new ArrayList<>();

}
