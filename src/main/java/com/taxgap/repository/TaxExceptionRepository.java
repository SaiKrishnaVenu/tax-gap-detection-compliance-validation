package com.taxgap.repository;

import com.taxgap.domain.entity.TaxException;
import com.taxgap.domain.enums.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxExceptionRepository extends JpaRepository<TaxException, Long> {
    List<TaxException> findByCustomerId(String customerId);
    List<TaxException> findBySeverity(Severity severity);
    List<TaxException> findByRuleName(String ruleName);
    List<TaxException> findByCustomerIdAndSeverity(String customerId, Severity severity);
    List<TaxException> findByCustomerIdAndRuleName(String customerId, String ruleName);

    @Query("SELECT e.customerId, COUNT(e) FROM TaxException e GROUP BY e.customerId")
    List<Object[]> countByCustomer();

    @Query("SELECT e.severity, COUNT(e) FROM TaxException e GROUP BY e.severity")
    List<Object[]> countBySeverity();
}
