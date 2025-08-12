package com.dbserver.votingchallenge.controller;

import com.dbserver.votingchallenge.dto.OpenSessionRequest;
import com.dbserver.votingchallenge.dto.SessionDTO;
import com.dbserver.votingchallenge.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(
        name = "Voting Sessions",
        description = "Endpoints for managing voting sessions"
)
@RestController
@RequestMapping("/api/v1/topics")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @Operation(
            summary = "Open a new voting session",
            description = """
        Creates a new voting session for the specified topic.
        A custom session duration can be provided; if not specified, the default system duration will be used.
        """,
            parameters = {
                    @Parameter(
                            name = "topicId",
                            description = "Long of the topic for which the session will be opened",
                            example = "1"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Voting session successfully opened",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SessionDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "404", description = "Topic not found")
            }
    )
    @PostMapping("/{topicId}/sessions")
    public ResponseEntity<SessionDTO> openSession(
            @PathVariable Long topicId,
            @RequestBody OpenSessionRequest request
    ) {
        SessionDTO created = sessionService.openSession(topicId, request.toDuration());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


}
