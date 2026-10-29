package com.helpdeskpro.helpdeskpro.repository;

import com.helpdeskpro.helpdeskpro.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.helpdeskpro.helpdeskpro.model.Role;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);
}