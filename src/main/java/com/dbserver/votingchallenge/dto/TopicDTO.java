package com.dbserver.votingchallenge.dto;

import com.dbserver.votingchallenge.enums.TopicStatus;
import java.time.LocalDateTime;

public record TopicDTO(
        Long topicId,
        String title,
        String description,
        TopicStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
