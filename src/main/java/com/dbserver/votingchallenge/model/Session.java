package com.dbserver.votingchallenge.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "session")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "session_seq")
    @SequenceGenerator(name = "session_seq", sequenceName = "session_seq", allocationSize = 1)
    @Column(name = "session_id")
    private Long sessionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_session_topic"))
    private Topic topic;

    @Column(name = "opens_at", nullable = false)
    private Instant opensAt;

    @Column(name = "closes_at", nullable = false)
    private Instant closesAt;

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }

    public Instant getOpensAt() { return opensAt; }
    public void setOpensAt(Instant opensAt) { this.opensAt = opensAt; }

    public Instant getClosesAt() { return closesAt; }
    public void setClosesAt(Instant closesAt) { this.closesAt = closesAt; }
}
