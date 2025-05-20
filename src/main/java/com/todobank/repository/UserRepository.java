package com.todobank.repository;

import com.todobank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByActive(Boolean active);


    Optional<User> findByPersonalNumber(String personalNumber);
}