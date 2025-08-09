package com.dbserver.votingchallenge.repository;

import com.dbserver.votingchallenge.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    @Query("""
            SELECT s FROM Session s
            WHERE s.topic.topicId = :topicId
              AND s.opensAt <= :now
              AND s.closesAt >= :now
            """)
    Optional<Session> findActiveByTopicId(@Param("topicId") Long topicId,
                                          @Param("now") Instant now);

}