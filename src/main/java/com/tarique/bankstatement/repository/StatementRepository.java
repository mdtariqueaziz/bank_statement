package com.tarique.bankstatement.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tarique.bankstatement.entity.Statement;

/**
 * StatementRepository — optimized for high-throughput date-range queries.
 *
 * <p>The JPQL query uses the composite index (account_id, transaction_date).
 * Pagination is handled via Spring Data's {@link Pageable} so only a page
 * of rows is fetched per request — crucial for 50M-row tables.
 */
@Repository
public interface StatementRepository extends JpaRepository<Statement, UUID> {

    /**
     * Primary endpoint query: paginated statements for an account within a date range.
     * Uses the covering index idx_stmt_account_date.
     */
    @Query("""
            SELECT s FROM Statement s
            WHERE s.accountId = :accountId
              AND s.transactionDate BETWEEN :from AND :to
            ORDER BY s.transactionDate DESC, s.id DESC
            """)
    Page<Statement> findByAccountIdAndDateRange(
            @Param("accountId") String accountId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable
    );

    /**
     * Count query for summary stats (used in response metadata).
     */
    @Query("""
            SELECT COUNT(s) FROM Statement s
            WHERE s.accountId = :accountId
              AND s.transactionDate BETWEEN :from AND :to
            """)
    long countByAccountIdAndDateRange(
            @Param("accountId") String accountId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    /**
     * Aggregated summary: total credits, total debits for the date range.
     */
    @Query("""
            SELECT
                SUM(CASE WHEN s.type = 'CREDIT' THEN s.amount ELSE 0 END),
                SUM(CASE WHEN s.type = 'DEBIT'  THEN s.amount ELSE 0 END)
            FROM Statement s
            WHERE s.accountId = :accountId
              AND s.transactionDate BETWEEN :from AND :to
            """)
    List<Object[]> getDebitCreditSummary(
            @Param("accountId") String accountId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}
