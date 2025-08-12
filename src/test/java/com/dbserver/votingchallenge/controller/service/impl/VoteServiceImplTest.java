package com.dbserver.votingchallenge.controller.service.impl;

import com.dbserver.votingchallenge.dto.ResultDTO;
import com.dbserver.votingchallenge.dto.VoteDTO;
import com.dbserver.votingchallenge.dto.VoteRequest;
import com.dbserver.votingchallenge.enums.VoteChoice;
import com.dbserver.votingchallenge.model.Associate;
import com.dbserver.votingchallenge.model.Topic;
import com.dbserver.votingchallenge.model.Session;
import com.dbserver.votingchallenge.model.Vote;
import com.dbserver.votingchallenge.repository.AssociateRepository;
import com.dbserver.votingchallenge.repository.SessionRepository;
import com.dbserver.votingchallenge.repository.TopicRepository;
import com.dbserver.votingchallenge.repository.VoteRepository;
import com.dbserver.votingchallenge.service.CpfValidationService;
import com.dbserver.votingchallenge.service.impl.VoteServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = VoteServiceImpl.class)
class VoteServiceImplTest {

    @MockBean
    private TopicRepository topicRepository;

    @MockBean
    private SessionRepository sessionRepository;

    @MockBean
    private VoteRepository voteRepository;

    @MockBean
    private AssociateRepository associateRepository;

    @MockBean
    private CpfValidationService cpfValidationService;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private VoteServiceImpl service;

    @Test
    void registerVote_success_flow() {
        Long topicId = 1L;
        String cpf = "11122233344";
        when(cpfValidationService.canVote(cpf)).thenReturn(true);

        Topic topic = new Topic();
        topic.setTopicId(topicId);
        topic.setTitle("Tema");
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
        when(sessionRepository.findActiveByTopicId(eq(topicId), any(LocalDateTime.class)))
                .thenReturn(Optional.of(new Session()));

        Associate assoc = new Associate();
        assoc.setAssociateId(10L);
        assoc.setExternalId(cpf);
        when(associateRepository.findByExternalId(cpf)).thenReturn(Optional.of(assoc));

        SetOperations<String, Object> setOps = mock(SetOperations.class);
        when(redisTemplate.opsForSet()).thenReturn(setOps);
        when(setOps.isMember("votes:" + topicId, 10L)).thenReturn(Boolean.FALSE);

        when(voteRepository.existsByTopic_TopicIdAndAssociate_AssociateId(topicId, 10L))
                .thenReturn(false);

        Vote saved = new Vote();
        saved.setVoteId(100L);
        saved.setTopic(topic);
        saved.setAssociate(assoc);
        saved.setChoice(VoteChoice.SIM);
        saved.setCreatedAt(LocalDateTime.now());
        when(voteRepository.save(any(Vote.class))).thenReturn(saved);

        VoteRequest req = new VoteRequest(cpf, VoteChoice.SIM);
        VoteDTO dto = service.registerVote(topicId, req);

        assertEquals(100L, dto.voteId());
        verify(setOps).add("votes:" + topicId, 10L);
        verify(redisTemplate).expire("votes:" + topicId, 24 * 60 * 60L, TimeUnit.SECONDS);
        verify(redisTemplate).delete("result:" + topicId);
    }

    @Test
    void registerVote_throwsConflict_whenDuplicateOnCache() {
        Long topicId = 2L;
        String cpf = "11122233344";
        when(cpfValidationService.canVote(cpf)).thenReturn(true);

        Topic topic = new Topic();
        topic.setTopicId(topicId);
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
        when(sessionRepository.findActiveByTopicId(eq(topicId), any(LocalDateTime.class)))
                .thenReturn(Optional.of(new Session()));

        Associate assoc = new Associate();
        assoc.setAssociateId(11L);
        assoc.setExternalId(cpf);
        when(associateRepository.findByExternalId(cpf)).thenReturn(Optional.of(assoc));

        SetOperations<String, Object> setOps = mock(SetOperations.class);
        when(redisTemplate.opsForSet()).thenReturn(setOps);
        when(setOps.isMember("votes:" + topicId, 11L)).thenReturn(Boolean.TRUE);

        VoteRequest req = new VoteRequest(cpf, VoteChoice.NAO);
        assertThrows(ResponseStatusException.class, () -> service.registerVote(topicId, req));
    }

    @Test
    void registerVote_throwsConflict_whenDuplicateOnDb_populatesCache() {
        Long topicId = 3L;
        String cpf = "11122233344";
        when(cpfValidationService.canVote(cpf)).thenReturn(true);

        Topic topic = new Topic();
        topic.setTopicId(topicId);
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
        when(sessionRepository.findActiveByTopicId(eq(topicId), any(LocalDateTime.class)))
                .thenReturn(Optional.of(new Session()));

        Associate assoc = new Associate();
        assoc.setAssociateId(12L);
        assoc.setExternalId(cpf);
        when(associateRepository.findByExternalId(cpf)).thenReturn(Optional.of(assoc));

        SetOperations<String, Object> setOps = mock(SetOperations.class);
        when(redisTemplate.opsForSet()).thenReturn(setOps);
        when(setOps.isMember("votes:" + topicId, 12L)).thenReturn(Boolean.FALSE);

        when(voteRepository.existsByTopic_TopicIdAndAssociate_AssociateId(topicId, 12L))
                .thenReturn(true);

        VoteRequest req = new VoteRequest(cpf, VoteChoice.SIM);
        assertThrows(ResponseStatusException.class, () -> service.registerVote(topicId, req));

        verify(setOps).add("votes:" + topicId, 12L);
        verify(redisTemplate).expire("votes:" + topicId, 24 * 60 * 60L, TimeUnit.SECONDS);
    }

    @Test
    void registerVote_throwsUnprocessable_whenNoActiveSession() {
        Long topicId = 4L;
        String cpf = "11122233344";
        when(cpfValidationService.canVote(cpf)).thenReturn(true);
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(new Topic()));
        when(sessionRepository.findActiveByTopicId(eq(topicId), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        VoteRequest req = new VoteRequest(cpf, VoteChoice.NAO);
        assertThrows(ResponseStatusException.class, () -> service.registerVote(topicId, req));
    }

    @Test
    void registerVote_throwsNotFound_whenCpfInvalid() {
        Long topicId = 5L;
        String cpf = "11122233344";
        when(cpfValidationService.canVote(cpf)).thenReturn(false);

        VoteRequest req = new VoteRequest(cpf, VoteChoice.SIM);
        assertThrows(ResponseStatusException.class, () -> service.registerVote(topicId, req));
    }

    @Test
    void getResult_returnsCached_whenHit() {
        Long topicId = 6L;
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        ResultDTO cached = new ResultDTO(topicId, 1L, 2L, 3L, "Não", "tema");
        when(valueOps.get("result:" + topicId)).thenReturn(cached);

        ResultDTO got = service.getResult(topicId);
        assertEquals(3L, got.totalVotes());
    }

    @Test
    void getResult_computesAndCaches_whenMiss() {
        Long topicId = 7L;
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("result:" + topicId)).thenReturn(null);

        when(voteRepository.countByTopic_TopicIdAndChoice(topicId, VoteChoice.SIM)).thenReturn(5L);
        when(voteRepository.countByTopic_TopicIdAndChoice(topicId, VoteChoice.NAO)).thenReturn(3L);

        Topic t = new Topic();
        t.setTopicId(topicId);
        t.setTitle("Assunto");
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(t));

        ResultDTO result = service.getResult(topicId);

        assertEquals(8L, result.totalVotes());
        verify(valueOps).set(eq("result:" + topicId), ArgumentMatchers.any(ResultDTO.class), eq(60L), eq(TimeUnit.SECONDS));
    }
}
