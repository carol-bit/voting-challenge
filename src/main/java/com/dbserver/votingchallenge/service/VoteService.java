package com.dbserver.votingchallenge.service;

import com.dbserver.votingchallenge.dto.ResultDTO;
import com.dbserver.votingchallenge.dto.VoteDTO;
import com.dbserver.votingchallenge.dto.VoteRequest;

public interface VoteService {
    VoteDTO registerVote(Long topicId, VoteRequest request);
    ResultDTO getResult(Long topicId);

}