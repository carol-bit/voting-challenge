package com.dbserver.votingchallenge.repository;

import com.dbserver.votingchallenge.enums.TopicStatus;
import com.dbserver.votingchallenge.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {}