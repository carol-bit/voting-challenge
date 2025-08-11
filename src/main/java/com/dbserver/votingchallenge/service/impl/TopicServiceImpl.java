package com.dbserver.votingchallenge.service.impl;

import com.dbserver.votingchallenge.dto.CreateTopicRequest;
import com.dbserver.votingchallenge.dto.TopicDTO;
import com.dbserver.votingchallenge.enums.TopicStatus;
import com.dbserver.votingchallenge.model.Topic;
import com.dbserver.votingchallenge.repository.TopicRepository;
import com.dbserver.votingchallenge.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;

    @Override
    @Transactional
    public TopicDTO createTopic(CreateTopicRequest request) {
        Topic topicEntity = new Topic();
        topicEntity.setTitle(request.title());
        topicEntity.setDescription(request.description());
        topicEntity.setStatus(TopicStatus.DRAFT);
        topicEntity.setCreatedAt(LocalDateTime.now());
        topicEntity.setUpdatedAt(LocalDateTime.now());

        topicEntity = topicRepository.save(topicEntity);
        return toDto(topicEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public TopicDTO getTopic(Long topicId) {
        return toDto(findTopicEntityById(topicId));
    }

    @Override
    @Transactional(readOnly = true)
    public Topic findTopicEntityById(Long topicId) {
        return topicRepository.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Topic not found"));
    }

    @Override
    @Transactional
    public void updateTopicStatus(Long topicId, TopicStatus status) {
        Topic topic = findTopicEntityById(topicId);
        topic.setStatus(status);
        topic.setUpdatedAt(LocalDateTime.now());
        topicRepository.save(topic);
    }

    private static TopicDTO toDto(Topic topic) {
        return new TopicDTO(
                topic.getTopicId(),
                topic.getTitle(),
                topic.getDescription(),
                topic.getStatus(),
                topic.getCreatedAt(),
                topic.getUpdatedAt()
        );
    }
}
