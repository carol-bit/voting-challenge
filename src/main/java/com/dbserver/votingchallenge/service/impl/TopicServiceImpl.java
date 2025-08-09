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

import java.time.Instant;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;

    @Override
    @Transactional
    public TopicDTO createTopic(CreateTopicRequest request) {
        Topic entity = new Topic();
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setStatus(TopicStatus.DRAFT);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        entity = topicRepository.save(entity);
        return toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public TopicDTO getTopic(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Topic not found"));
        return toDto(topic);
    }

    private static TopicDTO toDto(Topic t) {
        return new TopicDTO(
                t.getTopicId(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}
