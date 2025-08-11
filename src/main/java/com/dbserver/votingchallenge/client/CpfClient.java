package com.dbserver.votingchallenge.client;

import com.dbserver.votingchallenge.dto.CpfStatusResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "cpfClient", url = "${cpf.validation.url}")
public interface CpfClient {

    @GetMapping("/cpf/validate/{cpf}")
    CpfStatusResponse validateCpf(@PathVariable("cpf") String cpf);

}
