package com.dbserver.votingchallenge.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "associate",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_associate_external_id", columnNames = "external_id")
        })
@Getter
@Setter
@NoArgsConstructor
public class Associate {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "associate_seq")
    @SequenceGenerator(name = "associate_seq", sequenceName = "associate_seq", allocationSize = 1)
    @Column(name = "associate_id")
    private Long associateId;

    @Column(name = "external_id", nullable = false, length = 64)
    private String externalId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
