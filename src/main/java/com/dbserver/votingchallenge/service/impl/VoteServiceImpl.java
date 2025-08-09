package com.dbserver.votingchallenge.service.impl;

import com.dbserver.votingchallenge.dto.ResultDTO;
import com.dbserver.votingchallenge.dto.VoteDTO;
import com.dbserver.votingchallenge.dto.VoteRequest;
import com.dbserver.votingchallenge.enums.TopicStatus;
import com.dbserver.votingchallenge.enums.VoteChoice;
import com.dbserver.votingchallenge.model.Associate;
import com.dbserver.votingchallenge.model.Topic;
import com.dbserver.votingchallenge.model.Vote;
import com.dbserver.votingchallenge.repository.AssociateRepository;
import com.dbserver.votingchallenge.repository.SessionRepository;
import com.dbserver.votingchallenge.repository.TopicRepository;
import com.dbserver.votingchallenge.repository.VoteRepository;
import com.dbserver.votingchallenge.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class VoteServiceImpl implements VoteService {

    private final TopicRepository topicRepository;
    private final SessionRepository sessionRepository;
    private final VoteRepository voteRepository;
    private final AssociateRepository associateRepository;

    @Override
    @Transactional
    public VoteDTO registerVote(Long topicId, VoteRequest request) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "topic not found"));

        sessionRepository.findActiveByTopicId(topicId, Instant.now())
                .orElseThrow(() -> new ResponseStatusException(UNPROCESSABLE_ENTITY, "No active session for this topic"));

        Associate associate = associateRepository.findByExternalId(request.associateExternalId())
                .orElseGet(() -> {
                    Associate a = new Associate();
                    a.setExternalId(request.associateExternalId());
                    return associateRepository.save(a);
                });

        Vote vote = new Vote();
        vote.setTopic(topic);
        vote.setAssociate(associate);
        vote.setChoice(request.choice());
        vote.setCreatedAt(Instant.now());

        if (voteRepository.existsByTopic_TopicIdAndAssociate_AssociateId(topicId, associate.getAssociateId())) {
            throw new ResponseStatusException(CONFLICT, "Associate has already voted in this topic");
        }


        try {
            vote = voteRepository.save(vote);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(CONFLICT, "Associate has already voted in this topic");
        }

        return new VoteDTO(
                vote.getVoteId(),
                vote.getTopic().getTopicId(),
                vote.getAssociate().getExternalId(),
                vote.getChoice(),
                vote.getCreatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ResultDTO getResult(Long topicId) {
        long yes = voteRepository.countByTopic_TopicIdAndChoice(topicId, VoteChoice.SIM);
        long no  = voteRepository.countByTopic_TopicIdAndChoice(topicId, VoteChoice.NAO);

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Topic not found"));

        ResultDTO resultDTO = calculateResult(topicId, yes, no, topic.getTitle());
        return resultDTO;
    }

    public ResultDTO calculateResult(Long topicId, long yesVotes, long noVotes, String topicName) {
        long totalVotes = yesVotes + noVotes;
        String winner;

        if (yesVotes > noVotes) {
            winner = "YES";
        } else if (noVotes > yesVotes) {
            winner = "NO";
        } else {
            winner = "TIE";
        }

        return new ResultDTO(topicId, yesVotes, noVotes, totalVotes, winner, topicName);
    }
}
