package com.tarique.bankstatement.dto.response;

import com.tarique.bankstatement.entity.Statement;
import com.tarique.bankstatement.entity.enums.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Public-facing statement DTO — hides internal entity details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatementResponse {

    private UUID            id;
    private String          accountId;
    private LocalDate       transactionDate;
    private BigDecimal      amount;
    private TransactionType type;
    private String          description;
    private BigDecimal      balance;
    private String          referenceNumber;
    private String          currency;
    private LocalDateTime   createdAt;

    /** Map entity → DTO (no MapStruct dependency to keep it simple) */
    public static StatementResponse from(Statement s) {
        return StatementResponse.builder()
                .id(s.getId())
                .accountId(s.getAccountId())
                .transactionDate(s.getTransactionDate())
                .amount(s.getAmount())
                .type(s.getType())
                .description(s.getDescription())
                .balance(s.getBalance())
                .referenceNumber(s.getReferenceNumber())
                .currency(s.getCurrency())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
