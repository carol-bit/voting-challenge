package com.dbserver.votingchallenge.service.impl;

import com.dbserver.votingchallenge.client.CpfClient;
import com.dbserver.votingchallenge.dto.ResultDTO;
import com.dbserver.votingchallenge.dto.VoteDTO;
import com.dbserver.votingchallenge.dto.VoteRequest;
import com.dbserver.votingchallenge.enums.VoteChoice;
import com.dbserver.votingchallenge.model.Associate;
import com.dbserver.votingchallenge.model.Topic;
import com.dbserver.votingchallenge.model.Vote;
import com.dbserver.votingchallenge.repository.AssociateRepository;
import com.dbserver.votingchallenge.repository.SessionRepository;
import com.dbserver.votingchallenge.repository.TopicRepository;
import com.dbserver.votingchallenge.repository.VoteRepository;
import com.dbserver.votingchallenge.service.CpfValidationService;
import com.dbserver.votingchallenge.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Service
@RequiredArgsConstructor
public class VoteServiceImpl implements VoteService {

    private final TopicRepository topicRepository;
    private final SessionRepository sessionRepository;
    private final VoteRepository voteRepository;
    private final AssociateRepository associateRepository;

    private final CpfValidationService cpfValidationService;
    @Override
    @Transactional
    public VoteDTO registerVote(Long topicId, VoteRequest request) {

        if (!cpfValidationService.canVote(request.associateExternalId())) {
            throw new ResponseStatusException(NOT_FOUND, "CPF is not allowed to vote or invalid");
        }

        Topic topic = findTopicById(topicId);
        validateActiveSession(topicId);
        Associate associate = findOrCreateAssociate(request.associateExternalId());
        validateDuplicateVote(topicId, associate.getAssociateId());

        Vote vote = createVote(topic, associate, request.choice());
        Vote savedVote = saveVote(vote);

        return buildVoteDTO(savedVote);
    }

    @Override
    @Transactional(readOnly = true)
    public ResultDTO getResult(Long topicId) {
        long yes = countVotesByChoice(topicId, VoteChoice.SIM);
        long no = countVotesByChoice(topicId, VoteChoice.NAO);
        Topic topic = findTopicById(topicId);

        return calculateResult(topicId, yes, no, topic.getTitle());
    }

    private Topic findTopicById(Long topicId) {
        return topicRepository.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Topic not found"));
    }

    private void validateActiveSession(Long topicId) {
        LocalDateTime now = LocalDateTime.now();
        sessionRepository.findActiveByTopicId(topicId, now)
                .orElseThrow(() -> new ResponseStatusException(UNPROCESSABLE_ENTITY, "No active session for this topic"));
    }

    private Associate findOrCreateAssociate(String externalId) {
        Associate newAssociate = new Associate();
        newAssociate.setExternalId(externalId);
        return associateRepository.findByExternalId(externalId)
                .orElseGet(() -> associateRepository.save(newAssociate));
    }

    private void validateDuplicateVote(Long topicId, Long associateId) {
        if (voteRepository.existsByTopic_TopicIdAndAssociate_AssociateId(topicId, associateId)) {
            throw new ResponseStatusException(CONFLICT, "Associate has already voted in this topic");
        }
    }

    private Vote createVote(Topic topic, Associate associate, VoteChoice choice) {
        Vote vote = new Vote();
        vote.setTopic(topic);
        vote.setAssociate(associate);
        vote.setChoice(choice);
        vote.setCreatedAt(LocalDateTime.now());
        return vote;
    }

    private Vote saveVote(Vote vote) {
        try {
            return voteRepository.save(vote);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(CONFLICT, "Associate has already voted in this topic");
        }
    }

    private VoteDTO buildVoteDTO(Vote vote) {
        return new VoteDTO(
                vote.getVoteId(),
                vote.getTopic().getTopicId(),
                vote.getAssociate().getExternalId(),
                vote.getChoice(),
                vote.getCreatedAt()
        );
    }

    private long countVotesByChoice(Long topicId, VoteChoice choice) {
        return voteRepository.countByTopic_TopicIdAndChoice(topicId, choice);
    }

    public ResultDTO calculateResult(Long topicId, long yesVotes, long noVotes, String topicName) {
        long totalVotes = yesVotes + noVotes;
        String winner = determineWinner(yesVotes, noVotes);
        return new ResultDTO(topicId, yesVotes, noVotes, totalVotes, winner, topicName);
    }

    private String determineWinner(long yesVotes, long noVotes) {
        if (yesVotes > noVotes) return "Sim";
        if (noVotes > yesVotes) return "Não";
        return "Empate";
    }
}
