package com.taxgap.repository;

import com.taxgap.domain.entity.Transaction;
import com.taxgap.domain.enums.ValidationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionId(String transactionId);
    List<Transaction> findByCustomerId(String customerId);
    List<Transaction> findByValidationStatus(ValidationStatus status);
    boolean existsByTransactionId(String transactionId);

    @Query("SELECT t.customerId, COUNT(t), SUM(t.amount), SUM(t.reportedTax) " +
            "FROM Transaction t WHERE t.validationStatus = com.taxgap.domain.enums.ValidationStatus.SUCCESS " +
            "GROUP BY t.customerId")
    List<Object[]> findCustomerTransactionSummary();
}
