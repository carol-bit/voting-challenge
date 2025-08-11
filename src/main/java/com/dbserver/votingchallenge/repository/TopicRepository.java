package com.dbserver.votingchallenge.repository;

import com.dbserver.votingchallenge.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {}