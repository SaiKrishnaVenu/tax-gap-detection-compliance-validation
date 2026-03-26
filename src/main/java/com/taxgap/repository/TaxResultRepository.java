package com.taxgap.repository;

import com.taxgap.domain.entity.TaxResult;
import com.taxgap.domain.enums.ComplianceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaxResultRepository extends JpaRepository<TaxResult, Long> {
    Optional<TaxResult> findByTransactionId(String transactionId);
    List<TaxResult> findByCustomerId(String customerId);

    @Query("SELECT tr.customerId, " +
           "SUM(tr.amount), SUM(tr.reportedTax), SUM(tr.expectedTax), SUM(tr.taxGap), " +
           "COUNT(tr), SUM(CASE WHEN tr.complianceStatus != 'COMPLIANT' THEN 1 ELSE 0 END) " +
           "FROM TaxResult tr GROUP BY tr.customerId")
    List<Object[]> findCustomerTaxSummary();

    long countByCustomerIdAndComplianceStatus(String customerId, ComplianceStatus status);
    long countByCustomerId(String customerId);
}
