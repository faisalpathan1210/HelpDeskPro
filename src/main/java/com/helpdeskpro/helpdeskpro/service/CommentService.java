package com.helpdeskpro.helpdeskpro.service;

import com.helpdeskpro.helpdeskpro.model.Comment;
import com.helpdeskpro.helpdeskpro.model.Role;
import com.helpdeskpro.helpdeskpro.model.Ticket;
import com.helpdeskpro.helpdeskpro.model.User;
import com.helpdeskpro.helpdeskpro.repository.CommentRepository;
import com.helpdeskpro.helpdeskpro.repository.TicketRepository;
import com.helpdeskpro.helpdeskpro.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository,
                          TicketRepository ticketRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    public Comment addComment(Long ticketId, String message, String actorEmail) {
        if (message == null || message.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment cannot be empty");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        boolean isAdmin = actor.getRole() == Role.ADMIN;
        boolean isCreator = ticket.getCreatedBy().getId().equals(actor.getId());
        boolean isAssignee = ticket.getAssignedTo() != null
                && ticket.getAssignedTo().getId().equals(actor.getId());

        if (!isAdmin && !isCreator && !isAssignee) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot comment on this ticket");
        }

        Comment comment = new Comment();
        comment.setTicket(ticket);
        comment.setUser(actor);
        comment.setMessage(message.trim());
        return commentRepository.save(comment);
    }

    public List<Comment> getComments(Long ticketId) {
        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
    }
}