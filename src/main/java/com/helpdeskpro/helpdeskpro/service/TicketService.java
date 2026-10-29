package com.helpdeskpro.helpdeskpro.service;

import com.helpdeskpro.helpdeskpro.model.Role;
import com.helpdeskpro.helpdeskpro.model.Status;
import com.helpdeskpro.helpdeskpro.model.StatusHistory;
import com.helpdeskpro.helpdeskpro.model.Ticket;
import com.helpdeskpro.helpdeskpro.model.User;
import com.helpdeskpro.helpdeskpro.repository.StatusHistoryRepository;
import com.helpdeskpro.helpdeskpro.repository.TicketRepository;
import com.helpdeskpro.helpdeskpro.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    public TicketService(TicketRepository ticketRepository,
                         UserRepository userRepository,
                         StatusHistoryRepository statusHistoryRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    public Ticket createTicket(Ticket ticket, String creatorEmail) {
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + creatorEmail));
        ticket.setCreatedBy(creator);
        ticket.setStatus(Status.OPEN);
        return ticketRepository.save(ticket);
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + id));
    }

    public Ticket assignTicket(Long ticketId, Long supportUserId) {
        Ticket ticket = getTicketById(ticketId);

        if (ticket.getStatus() == Status.RESOLVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot assign a resolved ticket");
        }

        User support = userRepository.findById(supportUserId)
                .orElseThrow(() -> new RuntimeException("User not found: " + supportUserId));

        if (support.getRole() != Role.SUPPORT) {
            throw new RuntimeException("Tickets can only be assigned to SUPPORT users");
        }

        ticket.setAssignedTo(support);
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket updateStatus(Long ticketId, Status newStatus, String actorEmail) {
        Ticket ticket = getTicketById(ticketId);
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + actorEmail));

        // plain users can never change a status
        if (actor.getRole() == Role.USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Users cannot change ticket status");
        }

        // a SUPPORT user may only change tickets assigned to them
        if (actor.getRole() == Role.SUPPORT) {
            User assignee = ticket.getAssignedTo();
            if (assignee == null || !assignee.getId().equals(actor.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "This ticket is not assigned to you");
            }
        }

        // a ticket must have an owner before its status can change
        if (ticket.getAssignedTo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Assign the ticket to a support user before changing its status");
        }

        Status oldStatus = ticket.getStatus();
        if (!oldStatus.canMoveTo(newStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot move from " + oldStatus + " to " + newStatus);
        }

        ticket.setStatus(newStatus);

        StatusHistory history = new StatusHistory();
        history.setTicket(ticket);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(actor);
        statusHistoryRepository.save(history);

        return ticketRepository.save(ticket);
    }

    public List<Ticket> getTicketsFor(String email) {
        User actor = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        return switch (actor.getRole()) {
            case ADMIN -> ticketRepository.findAll();
            case SUPPORT -> ticketRepository.findByAssignedTo(actor);
            case USER -> ticketRepository.findByCreatedBy(actor);
        };
    }

    public Ticket getTicketForUser(Long id, String email) {
        Ticket ticket = getTicketById(id);
        User actor = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        boolean isAdmin = actor.getRole() == Role.ADMIN;
        boolean isCreator = ticket.getCreatedBy().getId().equals(actor.getId());
        boolean isAssignee = ticket.getAssignedTo() != null
                && ticket.getAssignedTo().getId().equals(actor.getId());

        if (!isAdmin && !isCreator && !isAssignee) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot view this ticket");
        }
        return ticket;
    }
}