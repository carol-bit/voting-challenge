package com.dbserver.votingchallenge.repository;


import com.dbserver.votingchallenge.enums.VoteChoice;
import com.dbserver.votingchallenge.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

   // 1 voto por pauta: pré-checagem
   boolean existsByTopic_TopicIdAndAssociate_AssociateId(Long topicId, Long associateId);

   // contagem por pauta + escolha
   long countByTopic_TopicIdAndChoice(Long topicId, VoteChoice choice);

   // buscar voto por id dentro da pauta
   Optional<Vote> findByVoteIdAndTopic_TopicId(Long voteId, Long topicId);

}

