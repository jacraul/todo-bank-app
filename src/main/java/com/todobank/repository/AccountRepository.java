package com.todobank.repository;

import com.todobank.entity.Account;
import com.todobank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUser(User user);
    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByUserEmail(String email);


    List<Account> findByActive(Boolean active);

    List<Account> findByUserAndActiveTrue(User user);
}