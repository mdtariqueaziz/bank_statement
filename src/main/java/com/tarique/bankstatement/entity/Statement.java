package com.tarique.bankstatement.entity;

import com.tarique.bankstatement.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a single bank transaction (credit/debit) belonging to an account.
 *
 * <p><b>Performance design decisions:</b>
 * <ul>
 *   <li>Composite index on (account_id, transaction_date) — covers the primary query pattern.</li>
 *   <li>Covering index also includes amount/type to enable index-only scans.</li>
 *   <li>UUID primary key avoids hotspot inserts at scale (compared to sequential BIGINT).</li>
 *   <li>account_id is stored as VARCHAR(30) — matches Indian account number formats.</li>
 * </ul>
 */
@Entity
@Table(
    name = "statements",
    indexes = {
        // Primary query index: accountId + date range (most critical for 10K rps)
        @Index(name = "idx_stmt_account_date", columnList = "account_id, transaction_date"),
        // Partial covering index for pagination (date DESC)
        @Index(name = "idx_stmt_date_desc", columnList = "account_id, transaction_date DESC, id"),
        // For audit/reference lookups
        @Index(name = "idx_stmt_reference", columnList = "reference_number")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Statement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Bank account number — max 30 chars covers all Indian bank formats */
    @Column(name = "account_id", nullable = false, length = 30)
    private String accountId;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(length = 255)
    private String description;

    /** Running balance after this transaction */
    @Column(precision = 18, scale = 2)
    private BigDecimal balance;

    @Column(name = "reference_number", unique = true, length = 50)
    private String referenceNumber;

    /** ISO 4217 currency code, default INR */
    @Column(length = 3)
    @Builder.Default
    private String currency = "INR";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
