package com.dbserver.votingchallenge.service;

import com.dbserver.votingchallenge.dto.TopicDTO;
import com.dbserver.votingchallenge.dto.CreateTopicRequest;

public interface TopicService {
    TopicDTO createTopic(CreateTopicRequest request);
    TopicDTO getTopic(Long topicId);
}
