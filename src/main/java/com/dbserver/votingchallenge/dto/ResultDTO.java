package com.dbserver.votingchallenge.dto;

public record ResultDTO(
        Long topicId,
        long yes,
        long no,
        long totalVotes,
        String winner,
        String topicName
) {}

