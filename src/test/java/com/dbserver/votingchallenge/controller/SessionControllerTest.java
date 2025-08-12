package com.dbserver.votingchallenge.controller;

import com.dbserver.votingchallenge.dto.OpenSessionRequest;
import com.dbserver.votingchallenge.dto.SessionDTO;
import com.dbserver.votingchallenge.service.SessionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class SessionControllerTest {

    private final SessionService sessionService = Mockito.mock(SessionService.class);
    private final SessionController sessionController = new SessionController(sessionService);

    @Test
    void openSession_ReturnsCreatedSession() {
        Long topicId = 1L;
        OpenSessionRequest request = Mockito.mock(OpenSessionRequest.class);
        SessionDTO sessionDTO = new SessionDTO(10L, topicId, null, null);

        when(request.toDuration()).thenReturn(Duration.ofSeconds(90));
        when(sessionService.openSession(topicId, Duration.ofSeconds(90))).thenReturn(sessionDTO);

        ResponseEntity<SessionDTO> response = sessionController.openSession(topicId, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(sessionDTO, response.getBody());
    }
}
