package com.todobank.repository;

import com.todobank.entity.Account;
import com.todobank.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFromAccountOrderByTransactionDateDesc(Account account);
    List<Transaction> findByRiskyTrue();
}