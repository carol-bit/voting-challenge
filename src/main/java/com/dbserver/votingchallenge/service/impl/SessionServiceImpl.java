package com.dbserver.votingchallenge.service.impl;

import com.dbserver.votingchallenge.dto.SessionDTO;
import com.dbserver.votingchallenge.enums.TopicStatus;
import com.dbserver.votingchallenge.model.Session;
import com.dbserver.votingchallenge.model.Topic;
import com.dbserver.votingchallenge.repository.SessionRepository;
import com.dbserver.votingchallenge.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.CONFLICT;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private static final long DEFAULT_DURATION_SECONDS = 60L;

    private final SessionRepository sessionRepository;
    private final TopicServiceImpl topicService;

    @Override
    @Transactional
    public SessionDTO openSession(Long topicId, Duration duration) {
        Duration effectiveDuration = getEffectiveDuration(duration);
        Topic topic = topicService.findTopicEntityById(topicId);
        validateNoActiveSession(topicId);
        Session session = createAndSaveSession(topic, effectiveDuration);
        topicService.updateTopicStatus(topicId, TopicStatus.OPEN);
        return toDto(session);
    }

    private Duration getEffectiveDuration(Duration duration) {
        return (duration != null) ? duration : Duration.ofSeconds(DEFAULT_DURATION_SECONDS);
    }

    private void validateNoActiveSession(Long topicId) {
        LocalDateTime now = LocalDateTime.now();
        sessionRepository.findActiveByTopicId(topicId, now).ifPresent(s -> {
            throw new ResponseStatusException(CONFLICT, "Session already open for this topic");
        });
    }

    private Session createAndSaveSession(Topic topic, Duration duration) {
        LocalDateTime now = LocalDateTime.now();
        Session session = new Session();
        session.setTopic(topic);
        session.setOpensAt(now);
        session.setClosesAt(now.plus(duration));
        return sessionRepository.save(session);
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
