package com.helpdeskpro.helpdeskpro.repository;

import com.helpdeskpro.helpdeskpro.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}