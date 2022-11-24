package com.matopohl.user_management.domain;

import com.matopohl.user_management.domain.listener.RevInfoListener;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@RevisionEntity(RevInfoListener.class)
@Table(name = "revinfo", schema = "audit")
@Entity
public class RevInfo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "revinfo_gen")
    @SequenceGenerator(name = "revinfo_gen", sequenceName = "revinfo_seq", schema = "audit", allocationSize = 1)
    @RevisionNumber
    @Column(columnDefinition = "integer")
    private int id;

    @RevisionTimestamp
    @Column(columnDefinition = "bigint")
    private long timestamp;

    @SuppressWarnings("unused")
    @Transient
    public Date getRevisionDate() {
        return new Date(timestamp);
    }

    @Column(name = "request_id", unique = true, columnDefinition = "uuid")
    private UUID requestId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "fk_log_id", unique = true, columnDefinition = "uuid")
    private Log log;

}
