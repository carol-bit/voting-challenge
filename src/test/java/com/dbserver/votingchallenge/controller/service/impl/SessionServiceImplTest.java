package com.dbserver.votingchallenge.controller.service.impl;

import com.dbserver.votingchallenge.dto.SessionDTO;
import com.dbserver.votingchallenge.enums.TopicStatus;
import com.dbserver.votingchallenge.model.Session;
import com.dbserver.votingchallenge.model.Topic;
import com.dbserver.votingchallenge.repository.SessionRepository;
import com.dbserver.votingchallenge.service.RedisService;
import com.dbserver.votingchallenge.service.impl.SessionServiceImpl;
import com.dbserver.votingchallenge.service.impl.TopicServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@SpringBootTest
class SessionServiceImplTest {

    @MockBean
    private SessionRepository sessionRepository;

    @MockBean
    private TopicServiceImpl topicService;

    @MockBean
    private RedisService<SessionDTO> redisService;

    @Autowired
    private SessionServiceImpl service;

    @Test
    void openSession_throwsConflict_whenCachedSessionExists() {
        Long topicId = 1L;
        when(redisService.get("session:" + topicId, SessionDTO.class)).thenReturn(new SessionDTO(10L, topicId, LocalDateTime.now(), LocalDateTime.now().plusMinutes(1)));

        assertThrows(ResponseStatusException.class, () -> service.openSession(topicId, Duration.ofSeconds(30)));
    }

    @Test
    void openSession_throwsConflict_whenActiveSessionOnDb() {
        Long topicId = 1L;
        when(redisService.get("session:" + topicId, SessionDTO.class)).thenReturn(null);
        when(sessionRepository.findActiveByTopicId(eq(topicId), any(LocalDateTime.class))).thenReturn(Optional.of(new Session()));

        assertThrows(ResponseStatusException.class, () -> service.openSession(topicId, Duration.ofSeconds(30)));
    }

    @Test
    void openSession_createsSavesUpdatesAndCaches_onSuccess() {
        Long topicId = 2L;
        Duration duration = Duration.ofSeconds(75);
        Topic topic = new Topic();
        topic.setTopicId(topicId);
        when(redisService.get("session:" + topicId, SessionDTO.class)).thenReturn(null);
        when(sessionRepository.findActiveByTopicId(eq(topicId), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(topicService.findTopicEntityById(topicId)).thenReturn(topic);

        Session toSave = new Session();
        toSave.setTopic(topic);
        toSave.setOpensAt(LocalDateTime.now());
        toSave.setClosesAt(LocalDateTime.now().plus(duration));
        Session saved = new Session();
        saved.setSessionId(55L);
        saved.setTopic(topic);
        saved.setOpensAt(toSave.getOpensAt());
        saved.setClosesAt(toSave.getClosesAt());
        when(sessionRepository.save(any(Session.class))).thenReturn(saved);

        SessionDTO dto = service.openSession(topicId, duration);

        assertEquals(55L, dto.sessionId());
        verify(topicService).updateTopicStatus(topicId, TopicStatus.OPEN);
        verify(redisService).set("session:" + topicId, dto, duration.getSeconds(), TimeUnit.SECONDS);
    }
}
