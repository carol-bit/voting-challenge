package com.dbserver.votingchallenge.repository;


import com.dbserver.votingchallenge.enums.VoteChoice;
import com.dbserver.votingchallenge.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

   boolean existsByTopic_TopicIdAndAssociate_AssociateId(Long topicId, Long associateId);

   long countByTopic_TopicIdAndChoice(Long topicId, VoteChoice choice);

   Optional<Vote> findByVoteIdAndTopic_TopicId(Long voteId, Long topicId);

}

