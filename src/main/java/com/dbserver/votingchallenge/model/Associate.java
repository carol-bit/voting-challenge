package com.dbserver.votingchallenge.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "associate",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_associate_external_id", columnNames = "external_id")
       })
public class Associate {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "associate_seq")
    @SequenceGenerator(name = "associate_seq", sequenceName = "associate_seq", allocationSize = 1)
    @Column(name = "associate_id")
    private Long associateId;

    @Column(name = "external_id", nullable = false, length = 64)
    private String externalId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getAssociateId() { return associateId; }
    public void setAssociateId(Long associateId) { this.associateId = associateId; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
