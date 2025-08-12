package com.dbserver.votingchallenge.controller;

import com.dbserver.votingchallenge.controller.TopicController;
import com.dbserver.votingchallenge.dto.CreateTopicRequest;
import com.dbserver.votingchallenge.dto.TopicDTO;
import com.dbserver.votingchallenge.enums.TopicStatus;
import com.dbserver.votingchallenge.service.TopicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class TopicControllerTest {

    private TopicService topicService;
    private TopicController topicController;

    @BeforeEach
    void setup() {
        topicService = Mockito.mock(TopicService.class);
        topicController = new TopicController(topicService);
    }

    @Test
    void createTopics_ReturnsCreatedTopic() {
        // Arrange
        CreateTopicRequest request = Mockito.mock(CreateTopicRequest.class);

        TopicDTO expectedTopicDTO = new TopicDTO(
                1L,
                "Example Title",
                "Example Description",
                TopicStatus.OPEN,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(topicService.createTopic(request)).thenReturn(expectedTopicDTO);

        // Act
        ResponseEntity<TopicDTO> response = topicController.createTopics(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedTopicDTO, response.getBody());

        verify(topicService, times(1)).createTopic(request);
    }
}

