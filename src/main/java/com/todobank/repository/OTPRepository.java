package com.todobank.repository;

import com.todobank.entity.OTP;
import com.todobank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OTPRepository extends JpaRepository<OTP, Long> {
    Optional<OTP> findByUser(User user);
    void deleteByUser(User user);
}