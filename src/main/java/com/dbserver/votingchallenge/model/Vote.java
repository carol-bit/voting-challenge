package com.dbserver.votingchallenge.model;

import com.dbserver.votingchallenge.enums.VoteChoice;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "vote",
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_vote_topic_associate", columnNames = {"topic_id", "associate_id"})
        })
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
    private Instant createdAt = Instant.now();

    public Long getVoteId() { return voteId; }
    public void setVoteId(Long voteId) { this.voteId = voteId; }

    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }

    public Associate getAssociate() { return associate; }
    public void setAssociate(Associate associate) { this.associate = associate; }

    public VoteChoice getChoice() { return choice; }
    public void setChoice(VoteChoice choice) { this.choice = choice; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
