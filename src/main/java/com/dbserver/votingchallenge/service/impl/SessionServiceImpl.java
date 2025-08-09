package com.dbserver.votingchallenge.service.impl;

import com.dbserver.votingchallenge.dto.SessionDTO;
import com.dbserver.votingchallenge.enums.TopicStatus;
import com.dbserver.votingchallenge.model.Session;
import com.dbserver.votingchallenge.model.Topic;
import com.dbserver.votingchallenge.repository.SessionRepository;
import com.dbserver.votingchallenge.repository.TopicRepository;
import com.dbserver.votingchallenge.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private static final long DEFAULT_DURATION_SECONDS = 60L;

    private final TopicRepository topicRepository;
    private final SessionRepository sessionRepository;

    @Override
    @Transactional
    public SessionDTO openSession(Long topicId, Duration duration) {
        Duration effective = (duration != null) ? duration : Duration.ofSeconds(DEFAULT_DURATION_SECONDS);

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Topic not found"));

        Instant now = Instant.now();
        sessionRepository.findActiveByTopicId(topicId, now).ifPresent(s -> {
            throw new ResponseStatusException(CONFLICT, "Session already open for this topic");
        });

        Session s = new Session();
        s.setTopic(topic);                    // RELACIONAMENTO, não setar id cru
        s.setOpensAt(now);
        s.setClosesAt(now.plus(effective));

        s = sessionRepository.save(s);

        topic.setStatus(TopicStatus.OPEN);
        topicRepository.save(topic);

        return new SessionDTO(
                s.getSessionId(),
                s.getTopic().getTopicId(),
                s.getOpensAt(),
                s.getClosesAt()
        );
    }

}
