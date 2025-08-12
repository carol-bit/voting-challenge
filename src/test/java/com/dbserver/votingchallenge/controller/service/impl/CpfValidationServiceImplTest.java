package com.dbserver.votingchallenge.controller.service.impl;

import com.dbserver.votingchallenge.client.CpfClient;
import com.dbserver.votingchallenge.dto.CpfStatusResponse;
import com.dbserver.votingchallenge.service.impl.CpfValidationServiceImpl;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.Charset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class CpfValidationServiceImplTest {

    @MockBean
    private CpfClient cpfClient;

    @Autowired
    private CpfValidationServiceImpl service;

    @Test
    void canVote_returnsTrue_whenAbleToVote() {
        String cpf = "12345678901";
        CpfStatusResponse resp = mock(CpfStatusResponse.class);
        when(resp.getStatus()).thenReturn("ABLE_TO_VOTE");
        when(cpfClient.validateCpf(cpf)).thenReturn(resp);

        boolean result = service.canVote(cpf);

        assertTrue(result);
        verify(cpfClient).validateCpf(cpf);
    }

    @Test
    void canVote_throwsNotFound_whenUnableToVote() {
        String cpf = "12345678901";
        CpfStatusResponse resp = mock(CpfStatusResponse.class);
        when(resp.getStatus()).thenReturn("UNABLE_TO_VOTE");
        when(cpfClient.validateCpf(cpf)).thenReturn(resp);

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.canVote(cpf));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("CPF is not allowed to vote"));
        verify(cpfClient).validateCpf(cpf);
    }

    @Test
    void canVote_throwsNotFound_whenUnknownStatus() {
        String cpf = "12345678901";
        CpfStatusResponse resp = mock(CpfStatusResponse.class);
        when(resp.getStatus()).thenReturn("SOMETHING_ELSE");
        when(cpfClient.validateCpf(cpf)).thenReturn(resp);

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.canVote(cpf));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Unknown CPF status"));
        verify(cpfClient).validateCpf(cpf);
    }

    @Test
    void canVote_throwsNotFound_whenCpfClientReturns404() {
        String cpf = "12345678901";

        Request request = Request.create(
                Request.HttpMethod.GET,
                "/cpfs/" + cpf,
                Map.of(),
                null,
                Charset.defaultCharset(),
                new RequestTemplate()
        );
        FeignException.NotFound notFound =
                new FeignException.NotFound("not found", request, null, Map.of());

        when(cpfClient.validateCpf(cpf)).thenThrow(notFound);

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> service.canVote(cpf));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("CPF not found"));
        verify(cpfClient).validateCpf(cpf);
    }
}
