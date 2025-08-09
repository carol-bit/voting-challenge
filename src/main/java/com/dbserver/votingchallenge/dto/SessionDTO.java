package com.dbserver.votingchallenge.dto;

import java.time.Instant;

public record SessionDTO(
        Long sessionId,
        Long topicId,
        Instant opensAt,
        Instant closesAt
) {}
