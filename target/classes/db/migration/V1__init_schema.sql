-- V1__init_schema.sql  (MySQL compatible)

CREATE TABLE IF NOT EXISTS users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL,
    enabled    TINYINT(1)   NOT NULL DEFAULT 1,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transactions (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id     VARCHAR(100) NOT NULL UNIQUE,
    date               DATE         NOT NULL,
    customer_id        VARCHAR(100) NOT NULL,
    amount             DECIMAL(19,4),
    tax_rate           DECIMAL(10,4),
    reported_tax       DECIMAL(19,4),
    transaction_type   VARCHAR(20)  NOT NULL,
    validation_status  VARCHAR(20)  NOT NULL,
    failure_reasons    TEXT,
    raw_payload        TEXT,
    created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_transactions_customer (customer_id),
    INDEX idx_transactions_status   (validation_status)
);

CREATE TABLE IF NOT EXISTS tax_results (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id    VARCHAR(100) NOT NULL UNIQUE,
    customer_id       VARCHAR(100) NOT NULL,
    amount            DECIMAL(19,4) NOT NULL,
    tax_rate          DECIMAL(10,4) NOT NULL,
    reported_tax      DECIMAL(19,4),
    expected_tax      DECIMAL(19,4) NOT NULL,
    tax_gap           DECIMAL(19,4) NOT NULL,
    compliance_status VARCHAR(20)   NOT NULL,
    computed_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tax_results_customer (customer_id),
    CONSTRAINT fk_tr_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id)
);

CREATE TABLE IF NOT EXISTS tax_rules (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name   VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    enabled     TINYINT(1)   NOT NULL DEFAULT 1,
    config_json TEXT         NOT NULL,
    severity    VARCHAR(10)  NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS exceptions (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(100) NOT NULL,
    customer_id    VARCHAR(100) NOT NULL,
    rule_name      VARCHAR(100) NOT NULL,
    severity       VARCHAR(10)  NOT NULL,
    message        TEXT         NOT NULL,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_exceptions_customer (customer_id),
    INDEX idx_exceptions_severity (severity),
    INDEX idx_exceptions_rule     (rule_name)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type     VARCHAR(30)  NOT NULL,
    transaction_id VARCHAR(100),
    detail_json    TEXT         NOT NULL,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_txn   (transaction_id),
    INDEX idx_audit_event (event_type)
);

-- ─── Seed users  (password = 'password' BCrypt encoded) ───
INSERT IGNORE INTO users (username, password, role) VALUES
('admin',   '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ROLE_ADMIN'),
('auditor', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ROLE_AUDITOR');

-- ─── Seed tax rules ───
INSERT IGNORE INTO tax_rules (rule_name, description, enabled, config_json, severity) VALUES
('HIGH_VALUE_TRANSACTION',
 'Flag transactions whose amount exceeds a configurable threshold',
 1, '{"threshold": 100000}', 'HIGH'),

('REFUND_VALIDATION',
 'Refund amount must not exceed the maximum allowed refund limit',
 1, '{"maxRefundAmount": 50000}', 'MEDIUM'),

('GST_SLAB_VIOLATION',
 'Amount above slab threshold must have tax rate >= minimum required slab rate',
 1, '{"slabThreshold": 10000, "minTaxRate": 0.18}', 'HIGH');
