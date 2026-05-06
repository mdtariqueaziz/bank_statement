package com.tarique.bankstatement.service;

import com.tarique.bankstatement.dto.response.PagedResponse;
import com.tarique.bankstatement.dto.response.StatementResponse;

import java.time.LocalDate;

public interface StatementService {

    /**
     * Fetch paginated bank statements for a given account within a date range.
     *
     * @param accountId the bank account identifier
     * @param from      start date (inclusive)
     * @param to        end date (inclusive)
     * @param page      page number (1-based from client, converted internally)
     * @param size      records per page
     * @param sortBy    field to sort by (transactionDate, amount)
     * @param sortDir   sort direction (asc, desc)
     * @return paginated response with summary stats
     */
    PagedResponse<StatementResponse> getStatements(
            String accountId,
            LocalDate from,
            LocalDate to,
            int page,
            int size,
            String sortBy,
            String sortDir
    );
}
