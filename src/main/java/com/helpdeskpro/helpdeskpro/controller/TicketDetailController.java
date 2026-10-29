package com.helpdeskpro.helpdeskpro.controller;

import com.helpdeskpro.helpdeskpro.model.Comment;
import com.helpdeskpro.helpdeskpro.model.StatusHistory;
import com.helpdeskpro.helpdeskpro.repository.StatusHistoryRepository;
import com.helpdeskpro.helpdeskpro.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}")
public class TicketDetailController {

    public record CommentRequest(String message) {}

    private final CommentService commentService;
    private final StatusHistoryRepository statusHistoryRepository;

    public TicketDetailController(CommentService commentService,
                                  StatusHistoryRepository statusHistoryRepository) {
        this.commentService = commentService;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    @PostMapping("/comments")
    public ResponseEntity<Comment> addComment(@PathVariable Long ticketId,
                                              @RequestBody CommentRequest request,
                                              Authentication authentication) {
        Comment saved = commentService.addComment(ticketId, request.message(), authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/comments")
    public List<Comment> getComments(@PathVariable Long ticketId) {
        return commentService.getComments(ticketId);
    }

    @GetMapping("/history")
    public List<StatusHistory> getHistory(@PathVariable Long ticketId) {
        return statusHistoryRepository.findByTicketIdOrderByChangedAtAsc(ticketId);
    }
}