package com.dbserver.votingchallenge.repository;

import com.dbserver.votingchallenge.model.Associate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssociateRepository extends JpaRepository<Associate, Long> {
    Optional<Associate> findByExternalId(String externalId);
}