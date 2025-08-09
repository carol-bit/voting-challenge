package com.dbserver.votingchallenge.dto;

import java.time.Instant;

public record VoteDTO(
        Long id,
        Long topicId,
        String associateId,
        com.dbserver.votingchallenge.enums.VoteChoice choice,
        Instant createdAt
) {}