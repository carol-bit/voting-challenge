package com.dbserver.votingchallenge.dto;

import com.dbserver.votingchallenge.enums.TopicStatus;
import java.time.Instant;

public record TopicDTO(
        Long topicId,
        String title,
        String description,
        TopicStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
