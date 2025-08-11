package com.dbserver.votingchallenge.service.impl;

import com.dbserver.votingchallenge.client.CpfClient;
import com.dbserver.votingchallenge.dto.CpfStatusResponse;
import com.dbserver.votingchallenge.service.CpfValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CpfValidationServiceImpl implements CpfValidationService {

    private final CpfClient cpfClient;

    @Override
    public boolean canVote(String cpf) {
        try {
            CpfStatusResponse response = cpfClient.validateCpf(cpf);

            if ("ABLE_TO_VOTE".equals(response.getStatus())) {
                return true;
            } else if ("UNABLE_TO_VOTE".equals(response.getStatus())) {
                throw new ResponseStatusException(NOT_FOUND, "CPF is not allowed to vote");
            } else {
                throw new ResponseStatusException(NOT_FOUND, "Unknown CPF status");
            }
        } catch (feign.FeignException.NotFound e) {
            throw new ResponseStatusException(NOT_FOUND, "CPF not found");
        }
    }
}
