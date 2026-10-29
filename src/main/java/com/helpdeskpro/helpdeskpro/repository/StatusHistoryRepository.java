package com.helpdeskpro.helpdeskpro.repository;

import com.helpdeskpro.helpdeskpro.model.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {

    List<StatusHistory> findByTicketIdOrderByChangedAtAsc(Long ticketId);
}