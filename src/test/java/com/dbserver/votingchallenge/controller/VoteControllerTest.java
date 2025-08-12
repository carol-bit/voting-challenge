package com.dbserver.votingchallenge.controller;

import com.dbserver.votingchallenge.dto.ResultDTO;
import com.dbserver.votingchallenge.dto.VoteDTO;
import com.dbserver.votingchallenge.dto.VoteRequest;
import com.dbserver.votingchallenge.service.VoteService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class VoteControllerTest {

    private final VoteService voteService = Mockito.mock(VoteService.class);
    private final VoteController voteController = new VoteController(voteService);

    @Test
    void registerVote_ReturnsAcceptedVote() {
        Long topicId = 1L;
        VoteRequest request = Mockito.mock(VoteRequest.class);
        VoteDTO voteDTO = new VoteDTO(1L, topicId, "associate-1", null, null);

        when(voteService.registerVote(topicId, request)).thenReturn(voteDTO);

        ResponseEntity<VoteDTO> response = voteController.registerVote(topicId, request);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals(voteDTO, response.getBody());
    }

    @Test
    void getResult_ReturnsResult() {
        Long topicId = 1L;
        ResultDTO resultDTO = new ResultDTO(topicId, 5, 3, 8, "Sim", "Some Topic");

        when(voteService.getResult(topicId)).thenReturn(resultDTO);

        ResponseEntity<ResultDTO> response = voteController.getResult(topicId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resultDTO, response.getBody());
    }
}
