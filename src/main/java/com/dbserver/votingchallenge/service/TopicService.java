package com.dbserver.votingchallenge.service;

import com.dbserver.votingchallenge.dto.TopicDTO;
import com.dbserver.votingchallenge.dto.CreateTopicRequest;
import com.dbserver.votingchallenge.enums.TopicStatus;
import com.dbserver.votingchallenge.model.Topic;
import org.springframework.transaction.annotation.Transactional;

public interface TopicService {
    TopicDTO createTopic(CreateTopicRequest request);
    TopicDTO getTopic(Long topicId);

    @Transactional(readOnly = true)
    Topic findTopicEntityById(Long topicId);

    @Transactional
    void updateTopicStatus(Long topicId, TopicStatus status);
}
