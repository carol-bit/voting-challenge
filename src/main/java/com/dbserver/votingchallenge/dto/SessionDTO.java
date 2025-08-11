package com.dbserver.votingchallenge.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public record SessionDTO(
        Long sessionId,
        Long topicId,
        LocalDateTime opensAt,
        LocalDateTime closesAt
) {}
