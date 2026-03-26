package com.taxgap.repository;

import com.taxgap.domain.entity.AuditLog;
import com.taxgap.domain.enums.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByTransactionId(String transactionId);
    List<AuditLog> findByEventType(EventType eventType);
}
