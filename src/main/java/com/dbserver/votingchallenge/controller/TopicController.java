package com.dbserver.votingchallenge.controller;

import com.dbserver.votingchallenge.dto.TopicDTO;
import com.dbserver.votingchallenge.dto.CreateTopicRequest;
import com.dbserver.votingchallenge.service.TopicService;
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
        name = "Topics",
        description = "Endpoints for managing voting topics"
)
@RestController
@RequestMapping("/api/v1/topics")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @Operation(
            summary = "Create a new topics",
            description = "Receives topics data and returns the created topics.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Topics successfully created",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TopicDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request data")
            }
    )
    @PostMapping
    public ResponseEntity<TopicDTO> createTopics(
            @RequestBody CreateTopicRequest request
    ) {
        TopicDTO created = topicService.createTopic(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
            summary = "Get topics by ID",
            description = "Retrieves topics details by its unique identifier.",
            parameters = {
                    @Parameter(
                            name = "topicId",
                            description = "Long of the topics to be retrieved",
                            example = "1"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Topic found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TopicDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Topic not found")
            }
    )
    @GetMapping("/{topicId}")
    public ResponseEntity<TopicDTO> getTopic(@PathVariable Long topicId) {
        return ResponseEntity.ok(topicService.getTopic(topicId));
    }
}
