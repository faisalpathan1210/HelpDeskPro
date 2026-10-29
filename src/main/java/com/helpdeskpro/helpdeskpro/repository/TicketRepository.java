package com.helpdeskpro.helpdeskpro.repository;

import com.helpdeskpro.helpdeskpro.model.Status;
import com.helpdeskpro.helpdeskpro.model.Ticket;
import com.helpdeskpro.helpdeskpro.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import com.helpdeskpro.helpdeskpro.model.Priority;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByCreatedBy(User user);

    List<Ticket> findByAssignedTo(User user);

    List<Ticket> findByStatus(Status status);

    long countByStatus(Status status);
    long countByPriority(Priority priority);
}