package com.dbserver.votingchallenge.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Duration;

public record OpenSessionRequest(
        Long duration
) {
        @JsonIgnore
        public Duration toDuration() {
                return duration != null ? Duration.ofSeconds(duration) : null;
        }
}
