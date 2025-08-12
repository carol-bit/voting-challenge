package com.dbserver.votingchallenge.service.impl;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoteServiceImpl implements VoteService {

    private final TopicRepository topicRepository;
    private final SessionRepository sessionRepository;
    private final VoteRepository voteRepository;
    private final AssociateRepository associateRepository;

    private final CpfValidationService cpfValidationService;

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String RESULT_CACHE_PREFIX = "result:";
    private static final String VOTES_SET_PREFIX = "votes:";
    private static final long RESULT_CACHE_TTL = 60; // seconds
    private static final long VOTES_SET_TTL = 24 * 60 * 60; // 24 hours

    @Override
    @Transactional
    public VoteDTO registerVote(Long topicId, VoteRequest request) {
        log.info("Registering vote for topicId={} associateExternalId={}", topicId, request.associateExternalId());

        if (!cpfValidationService.canVote(request.associateExternalId())) {
            throw new ResponseStatusException(NOT_FOUND, "CPF is not allowed to vote or invalid");
        }

        Topic topic = findTopicById(topicId);
        validateActiveSession(topicId);

        Associate associate = findOrCreateAssociate(request.associateExternalId());

        validateDuplicateVoteWithCache(topicId, associate.getAssociateId());

        Vote vote = createVote(topic, associate, request.choice());
        Vote savedVote = saveVote(vote);

        addAssociateIdToVotesSet(topicId, associate.getAssociateId());
        invalidateResultCache(topicId);

        return buildVoteDTO(savedVote);
    }

    @Override
    @Transactional(readOnly = true)
    public ResultDTO getResult(Long topicId) {
        String cacheKey = RESULT_CACHE_PREFIX + topicId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached instanceof ResultDTO) {
            log.info("Returning cached result for topicId={}", topicId);
            return (ResultDTO) cached;
        }

        log.info("Cache miss for result of topicId={}, calculating...", topicId);
        long yesVotes = countVotesByChoice(topicId, VoteChoice.SIM);
        long noVotes = countVotesByChoice(topicId, VoteChoice.NAO);
        Topic topic = findTopicById(topicId);

        ResultDTO result = calculateResult(topicId, yesVotes, noVotes, topic.getTitle());

        redisTemplate.opsForValue().set(cacheKey, result, RESULT_CACHE_TTL, TimeUnit.SECONDS);
        log.debug("Cached result for topicId={} with TTL={} seconds", topicId, RESULT_CACHE_TTL);

        return result;
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

    private void validateDuplicateVoteWithCache(Long topicId, Long associateId) {
        String votesSetKey = VOTES_SET_PREFIX + topicId;

        Boolean hasVotedInCache = redisTemplate.opsForSet().isMember(votesSetKey, associateId);
        if (Boolean.TRUE.equals(hasVotedInCache)) {
            log.warn("Associate {} already voted for topicId={} (cache)", associateId, topicId);
            throw new ResponseStatusException(CONFLICT, "Associate has already voted in this topic");
        }

        boolean hasVotedInDb = voteRepository.existsByTopic_TopicIdAndAssociate_AssociateId(topicId, associateId);
        if (hasVotedInDb) {
            // populate cache to avoid DB next time
            redisTemplate.opsForSet().add(votesSetKey, associateId);
            redisTemplate.expire(votesSetKey, VOTES_SET_TTL, TimeUnit.SECONDS);
            log.warn("Associate {} already voted for topicId={} (DB)", associateId, topicId);
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
        } catch (DataIntegrityViolationException ex) {
            log.warn("DataIntegrityViolationException on saving vote: {}", ex.getMessage());
            throw new ResponseStatusException(CONFLICT, "Associate has already voted in this topic");
        }
    }

    private void addAssociateIdToVotesSet(Long topicId, Long associateId) {
        String votesSetKey = VOTES_SET_PREFIX + topicId;
        redisTemplate.opsForSet().add(votesSetKey, associateId);
        redisTemplate.expire(votesSetKey, VOTES_SET_TTL, TimeUnit.SECONDS);
        log.debug("Added associateId={} to Redis votes set for topicId={}", associateId, topicId);
    }

    private void invalidateResultCache(Long topicId) {
        String cacheKey = RESULT_CACHE_PREFIX + topicId;
        redisTemplate.delete(cacheKey);
        log.debug("Invalidated cached result for topicId={}", topicId);
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
