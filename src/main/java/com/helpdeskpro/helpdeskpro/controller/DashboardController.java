package com.helpdeskpro.helpdeskpro.controller;

import com.helpdeskpro.helpdeskpro.model.Priority;
import com.helpdeskpro.helpdeskpro.model.Status;
import com.helpdeskpro.helpdeskpro.model.Ticket;
import com.helpdeskpro.helpdeskpro.service.TicketService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final TicketService ticketService;

    public DashboardController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public Map<String, Object> dashboard(Authentication authentication) {
        List<Ticket> visible = ticketService.getTicketsFor(authentication.getName());

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (Status s : Status.values()) {
            byStatus.put(s.name(), visible.stream().filter(t -> t.getStatus() == s).count());
        }

        Map<String, Long> byPriority = new LinkedHashMap<>();
        for (Priority p : Priority.values()) {
            byPriority.put(p.name(), visible.stream().filter(t -> t.getPriority() == p).count());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", visible.size());
        result.put("byStatus", byStatus);
        result.put("byPriority", byPriority);
        return result;
    }
}