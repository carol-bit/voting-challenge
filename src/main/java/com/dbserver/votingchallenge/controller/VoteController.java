package com.dbserver.votingchallenge.controller;

import com.dbserver.votingchallenge.dto.ResultDTO;
import com.dbserver.votingchallenge.dto.VoteDTO;
import com.dbserver.votingchallenge.dto.VoteRequest;
import com.dbserver.votingchallenge.service.VoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(
        name = "Votes",
        description = "Endpoints for registering votes and retrieving voting results"
)
@RestController
@RequestMapping("/api/v1/topics")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @Operation(
            summary = "Register a vote for an topic",
            description = """
            Records a vote from a member for the specified topic.
            Each member can only vote once per topic.
            """,
            parameters = {
                    @Parameter(
                            name = "topicId",
                            description = "Long of the topic where the vote will be registered",
                            example = "1"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "202",
                            description = "Vote successfully registered",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = VoteDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request data or duplicate vote"),
                    @ApiResponse(responseCode = "404", description = "Topic not found")
            }
    )
    @PostMapping("/{topicId}/votes")
    public ResponseEntity<VoteDTO> registerVote(
            @PathVariable Long topicId,
            @RequestBody VoteRequest request
    ) {
        VoteDTO created = voteService.registerVote(topicId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(created);
    }

    @Operation(
            summary = "Get voting results for an topic",
            description = "Retrieves the final vote counts (yes/no) for the specified topic.",
            parameters = {
                    @Parameter(
                            name = "topicId",
                            description = "Long of the topic",
                            example = "1"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Voting results retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResultDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Topic not found")
            }
    )
    @GetMapping("/{topicId}/result")
    public ResponseEntity<ResultDTO> getResult(@PathVariable Long topicId) {
        return ResponseEntity.ok(voteService.getResult(topicId));
    }
}
