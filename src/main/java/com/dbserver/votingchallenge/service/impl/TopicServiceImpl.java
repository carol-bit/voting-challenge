package com.dbserver.votingchallenge.service.impl;

import com.dbserver.votingchallenge.dto.CreateTopicRequest;
import com.dbserver.votingchallenge.dto.TopicDTO;
import com.dbserver.votingchallenge.enums.TopicStatus;
import com.dbserver.votingchallenge.model.Topic;
import com.dbserver.votingchallenge.repository.TopicRepository;
import com.dbserver.votingchallenge.service.RedisService;
import com.dbserver.votingchallenge.service.TopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;
    private final RedisService<TopicDTO> redisService;

    private static final String TOPIC_CACHE_KEY = "topic::";
    private static final long CACHE_TTL_MINUTES = 10;

    @Override
    @Transactional
    public TopicDTO createTopic(CreateTopicRequest request) {
        log.info("Starting process to create a new topic: {}", request.title());
        Topic topicEntity = buildNewTopicEntity(request);
        topicEntity = topicRepository.save(topicEntity);
        TopicDTO dto = toDto(topicEntity);
        cacheTopic(dto);
        log.info("Topic successfully created with ID: {}", dto.topicId());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public TopicDTO getTopic(Long topicId) {
        log.info("Request to fetch topic with ID: {}", topicId);
        TopicDTO cachedTopic = getFromCache(topicId);
        if (cachedTopic != null) {
            log.info("Cache hit for topic ID {}", topicId);
            return cachedTopic;
        }
        log.warn("Cache miss for topic ID {}. Fetching from database...", topicId);
        TopicDTO topicDTO = toDto(findTopicEntityById(topicId));
        cacheTopic(topicDTO);
        return topicDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public Topic findTopicEntityById(Long topicId) {
        log.debug("Looking up topic entity in database for ID: {}", topicId);
        return topicRepository.findById(topicId)
                .orElseThrow(() -> {
                    log.error("Topic with ID {} not found", topicId);
                    return new ResponseStatusException(NOT_FOUND, "Topic not found");
                });
    }

    @Override
    @Transactional
    public void updateTopicStatus(Long topicId, TopicStatus status) {
        log.info("Updating status of topic ID {} to {}", topicId, status);
        Topic topic = findTopicEntityById(topicId);
        applyStatusUpdate(topic, status);
        topicRepository.save(topic);
        cacheTopic(toDto(topic));
        log.info("Status updated and cached for topic ID {}", topicId);
    }

    private Topic buildNewTopicEntity(CreateTopicRequest request) {
        Topic topic = new Topic();
        topic.setTitle(request.title());
        topic.setDescription(request.description());
        topic.setStatus(TopicStatus.DRAFT);
        topic.setCreatedAt(LocalDateTime.now());
        topic.setUpdatedAt(LocalDateTime.now());
        return topic;
    }

    private TopicDTO getFromCache(Long topicId) {
        String key = buildCacheKey(topicId);
        return redisService.get(key, TopicDTO.class);
    }

    private void cacheTopic(TopicDTO dto) {
        String key = buildCacheKey(dto.topicId());
        redisService.set(key, dto, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        log.debug("Topic cached in Redis with key: {}", key);
    }

    private String buildCacheKey(Long topicId) {
        return TOPIC_CACHE_KEY + topicId;
    }

    private void applyStatusUpdate(Topic topic, TopicStatus status) {
        topic.setStatus(status);
        topic.setUpdatedAt(LocalDateTime.now());
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
