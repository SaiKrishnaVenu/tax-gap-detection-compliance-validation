package com.taxgap.domain.entity;

import com.taxgap.domain.enums.ComplianceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tax_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false, unique = true)
    private String transactionId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Column(name = "tax_rate", precision = 10, scale = 4, nullable = false)
    private BigDecimal taxRate;

    @Column(name = "reported_tax", precision = 19, scale = 4)
    private BigDecimal reportedTax;

    @Column(name = "expected_tax", precision = 19, scale = 4, nullable = false)
    private BigDecimal expectedTax;

    @Column(name = "tax_gap", precision = 19, scale = 4, nullable = false)
    private BigDecimal taxGap;

    @Enumerated(EnumType.STRING)
    @Column(name = "compliance_status", nullable = false)
    private ComplianceStatus complianceStatus;

    @Column(name = "computed_at")
    private LocalDateTime computedAt;

    @PrePersist
    public void prePersist() {

        this.computedAt = LocalDateTime.now();
    }
}
