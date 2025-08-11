package com.dbserver.votingchallenge.model;

import com.dbserver.votingchallenge.enums.VoteChoice;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "vote",
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_vote_topic_associate", columnNames = {"topic_id", "associate_id"})
        })
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vote_seq")
    @SequenceGenerator(name = "vote_seq", sequenceName = "vote_seq", allocationSize = 1)
    @Column(name = "vote_id")
    private Long voteId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_vote_topic"))
    private Topic topic;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "associate_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_vote_associate"))
    private Associate associate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private VoteChoice choice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
