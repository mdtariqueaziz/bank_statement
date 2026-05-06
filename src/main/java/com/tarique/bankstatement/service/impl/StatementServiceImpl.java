package com.tarique.bankstatement.service.impl;

import com.tarique.bankstatement.dto.response.PagedResponse;
import com.tarique.bankstatement.dto.response.StatementResponse;
import com.tarique.bankstatement.entity.Statement;
import com.tarique.bankstatement.repository.StatementRepository;
import com.tarique.bankstatement.service.StatementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * StatementServiceImpl — core business logic for the bank statement endpoint.
 *
 * <p><b>Performance strategy:</b>
 * <ul>
 *   <li>Result sets are cached with Caffeine (TTL 5 min) keyed on
 *       accountId + from + to + page + size — avoids repeated DB hits
 *       for the same hot accounts.</li>
 *   <li>DB query uses the composite index (account_id, transaction_date).</li>
 *   <li>Transaction is read-only → Hibernate skips dirty checking; connection
 *       pool returns the connection faster.</li>
 *   <li>Date range is validated (max 1 year) to prevent unbounded queries.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatementServiceImpl implements StatementService {

    private static final int MAX_DATE_RANGE_DAYS = 365;
    private static final int MAX_PAGE_SIZE       = 500;

    private final StatementRepository statementRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
        cacheNames = "statements",
        key         = "#accountId + '_' + #from + '_' + #to + '_' + #page + '_' + #size",
        unless      = "#result == null"
    )
    public PagedResponse<StatementResponse> getStatements(
            String accountId,
            LocalDate from,
            LocalDate to,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        log.info("Fetching statements for account={}, from={}, to={}, page={}, size={}",
                accountId, from, to, page, size);

        // ---- Input Sanitization ----------------------------------------
        validateDateRange(from, to);
        int safeSize  = Math.min(size, MAX_PAGE_SIZE);
        int pageIndex = Math.max(0, page - 1); // convert 1-based to 0-based

        // ---- Build Pageable -------------------------------------------
        Sort sort = buildSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(pageIndex, safeSize, sort);

        // ---- DB Query (uses composite index) --------------------------
        Page<Statement> statementPage =
                statementRepository.findByAccountIdAndDateRange(accountId, from, to, pageable);

        // ---- Map to DTO -----------------------------------------------
        List<StatementResponse> content = statementPage.getContent()
                .stream()
                .map(StatementResponse::from)
                .collect(Collectors.toList());

        // ---- Summary Stats (single aggregation query) -----------------
        List<Object[]> summaryRows =
                statementRepository.getDebitCreditSummary(accountId, from, to);

        BigDecimal totalCredits = BigDecimal.ZERO;
        BigDecimal totalDebits  = BigDecimal.ZERO;

        if (summaryRows != null && !summaryRows.isEmpty() && summaryRows.get(0) != null) {
            Object[] row = summaryRows.get(0);
            totalCredits = row[0] != null ? (BigDecimal) row[0] : BigDecimal.ZERO;
            totalDebits  = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
        }

        PagedResponse.SummaryStats stats = PagedResponse.SummaryStats.builder()
                .totalCredits(totalCredits)
                .totalDebits(totalDebits)
                .netBalance(totalCredits.subtract(totalDebits))
                .transactionCount(statementPage.getTotalElements())
                .build();

        return PagedResponse.<StatementResponse>builder()
                .content(content)
                .page(page)
                .size(safeSize)
                .totalElements(statementPage.getTotalElements())
                .totalPages(statementPage.getTotalPages())
                .first(statementPage.isFirst())
                .last(statementPage.isLast())
                .summary(stats)
                .build();
    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                              //
    // ------------------------------------------------------------------ //

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Date parameters 'from' and 'to' are required");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' date must not be after 'to' date");
        }
        long days = ChronoUnit.DAYS.between(from, to);
        if (days > MAX_DATE_RANGE_DAYS) {
            throw new IllegalArgumentException(
                    "Date range must not exceed " + MAX_DATE_RANGE_DAYS + " days");
        }
    }

    private Sort buildSort(String sortBy, String sortDir) {
        String field = switch (sortBy == null ? "" : sortBy.toLowerCase()) {
            case "amount" -> "amount";
            case "type"   -> "type";
            default       -> "transactionDate";
        };
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        // Secondary sort by id ensures stable ordering for pagination
        return Sort.by(direction, field).and(Sort.by(Sort.Direction.DESC, "id"));
    }
}
