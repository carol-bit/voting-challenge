package com.dbserver.votingchallenge.service.impl;

import com.dbserver.votingchallenge.dto.SessionDTO;
import com.dbserver.votingchallenge.enums.TopicStatus;
import com.dbserver.votingchallenge.model.Session;
import com.dbserver.votingchallenge.model.Topic;
import com.dbserver.votingchallenge.repository.SessionRepository;
import com.dbserver.votingchallenge.service.RedisService;
import com.dbserver.votingchallenge.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpStatus.CONFLICT;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private static final long DEFAULT_DURATION_SECONDS = 60L;
    private static final String SESSION_CACHE_KEY_PREFIX = "session:";

    private final SessionRepository sessionRepository;
    private final TopicServiceImpl topicService;
    private final RedisService<SessionDTO> redisService;

    @Override
    @Transactional
    public SessionDTO openSession(Long topicId, Duration duration) {
        log.info("Opening session for topicId={}", topicId);

        Duration effectiveDuration = (duration != null) ? duration : Duration.ofMinutes(1);

        Topic topic = topicService.findTopicEntityById(topicId);

        checkNoActiveSessionWithCache(topicId);

        Session session = createAndSaveSession(topic, effectiveDuration);
        topicService.updateTopicStatus(topicId, TopicStatus.OPEN);

        SessionDTO sessionDTO = toDto(session);

        cacheSession(topicId, sessionDTO, effectiveDuration);

        return sessionDTO;
    }

    private Duration getEffectiveDuration(Duration duration) {
        return (duration != null) ? duration : Duration.ofSeconds(DEFAULT_DURATION_SECONDS);
    }

    private void checkNoActiveSessionWithCache(Long topicId) {
        String cacheKey = SESSION_CACHE_KEY_PREFIX + topicId;

        SessionDTO cachedSession = redisService.get(cacheKey, SessionDTO.class);
        if (cachedSession != null) {
            log.warn("Active session found in cache for topicId={}", topicId);
            throw new ResponseStatusException(CONFLICT, "Session already open for this topic");
        }

        log.debug("No session found in cache for topicId={}, checking database", topicId);

        LocalDateTime now = LocalDateTime.now();
        Optional<Session> activeSession = sessionRepository.findActiveByTopicId(topicId, now);
        if (activeSession.isPresent()) {
            log.warn("Active session found in DB for topicId={}", topicId);
            throw new ResponseStatusException(CONFLICT, "Session already open for this topic");
        }
    }

    private Session createAndSaveSession(Topic topic, Duration duration) {
        LocalDateTime now = LocalDateTime.now();
        Session session = new Session();
        session.setTopic(topic);
        session.setOpensAt(now);
        session.setClosesAt(now.plus(duration));
        Session savedSession = sessionRepository.save(session);
        log.info("Session created and saved with id={} for topicId={}", savedSession.getSessionId(), topic.getTopicId());
        return savedSession;
    }

    private void cacheSession(Long topicId, SessionDTO sessionDTO, Duration duration) {
        String cacheKey = SESSION_CACHE_KEY_PREFIX + topicId;
        long ttlSeconds = duration.getSeconds();

        redisService.set(cacheKey, sessionDTO, ttlSeconds, TimeUnit.SECONDS);
        log.debug("Cached session for topicId={} with TTL={} seconds", topicId, ttlSeconds);
    }

    private SessionDTO toDto(Session session) {
        return new SessionDTO(
                session.getSessionId(),
                session.getTopic().getTopicId(),
                session.getOpensAt(),
                session.getClosesAt()
        );
    }
}
