package com.matopohl.user_management.domain;

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
@Audited
@Table(name = "resources", schema = "public", uniqueConstraints = {@UniqueConstraint(columnNames = {"request_url", "request_method"})})
@Entity
public class Resource {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "resource_id", nullable = false, unique = true, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "request_url", nullable = false, columnDefinition = "text")
    private String requestUrl;

    @Column(name = "request_method", nullable = false, columnDefinition = "text")
    private String requestMethod;

    @ManyToMany
    @JoinTable(name = "resources_authorities", joinColumns = {@JoinColumn(name = "fk_resource_id", referencedColumnName = "resource_id")},
            inverseJoinColumns = {@JoinColumn(name = "fk_authority_id", referencedColumnName = "authority_id")})
    private List<Authority> resourceAuthorities = new ArrayList<>();

}
