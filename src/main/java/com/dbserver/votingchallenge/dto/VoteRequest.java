package com.dbserver.votingchallenge.dto;

import com.dbserver.votingchallenge.enums.VoteChoice;

public record VoteRequest(
        String associateExternalId,
        VoteChoice choice
) {}
