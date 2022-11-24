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
@Table(name = "documents", schema = "public")
@Entity
public class Document implements java.io.Serializable{

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "document_id", nullable = false, unique = true, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "content", columnDefinition = "bytea")
    private byte[] content;
}
