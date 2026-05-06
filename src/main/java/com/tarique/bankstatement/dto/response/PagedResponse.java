package com.tarique.bankstatement.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

/**
 * Paginated response wrapper — carries page metadata alongside the content.
 * Used by the statements endpoint so clients know total pages, total records, etc.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedResponse<T> {

    private List<T> content;

    /** Current page number (0-based internally, exposed as 1-based to clients) */
    private int page;

    private int size;

    private long totalElements;

    private int totalPages;

    private boolean first;

    private boolean last;

    /** Summary block for the fetched date range */
    private SummaryStats summary;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SummaryStats {
        private java.math.BigDecimal totalCredits;
        private java.math.BigDecimal totalDebits;
        private java.math.BigDecimal netBalance;
        private long transactionCount;
    }
}
