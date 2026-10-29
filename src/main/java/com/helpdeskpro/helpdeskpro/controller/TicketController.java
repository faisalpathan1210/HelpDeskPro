package com.helpdeskpro.helpdeskpro.controller;

import com.helpdeskpro.helpdeskpro.model.Status;
import com.helpdeskpro.helpdeskpro.model.Ticket;
import com.helpdeskpro.helpdeskpro.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<Ticket> createTicket(@Valid @RequestBody Ticket ticket,
                                               Authentication authentication) {
        Ticket saved = ticketService.createTicket(ticket, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public List<Ticket> getAllTickets(Authentication authentication) {
        return ticketService.getTicketsFor(authentication.getName());
    }

    @GetMapping("/{id}")
    public Ticket getTicket(@PathVariable Long id, Authentication authentication) {
        return ticketService.getTicketForUser(id, authentication.getName());
    }

    @PatchMapping("/{id}/assign")
    public Ticket assignTicket(@PathVariable Long id,
                               @RequestParam Long supportId) {
        return ticketService.assignTicket(id, supportId);
    }

    @PatchMapping("/{id}/status")
    public Ticket updateStatus(@PathVariable Long id,
                               @RequestParam Status status,
                               Authentication authentication) {
        return ticketService.updateStatus(id, status, authentication.getName());
    }
}