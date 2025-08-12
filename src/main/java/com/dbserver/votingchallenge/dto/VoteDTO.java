package com.dbserver.votingchallenge.dto;

import com.dbserver.votingchallenge.enums.VoteChoice;

import java.time.LocalDateTime;

public record VoteDTO(
        Long voteId,
        Long topicId,
        String associateId,
        VoteChoice choice,
        LocalDateTime createdAt
) {}