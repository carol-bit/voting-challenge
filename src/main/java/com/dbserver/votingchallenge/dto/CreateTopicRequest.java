package com.dbserver.votingchallenge.dto;

public record CreateTopicRequest(
        String title,
        String description
) {}
