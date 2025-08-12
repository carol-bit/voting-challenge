package com.dbserver.votingchallenge.service;


import com.dbserver.votingchallenge.dto.SessionDTO;

import java.time.Duration;


public interface SessionService {
    SessionDTO openSession(Long topicId, Duration request);

}
