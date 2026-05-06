//package com.tarique.bankstatement.repository;
//
//import com.tarique.bankstatement.entity.Statement;
//import com.tarique.bankstatement.entity.enums.TransactionType;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
///**
// * Repository slice test — uses H2 in-memory DB (configured via application-test.yml).
// * Validates JPQL queries without mocking.
// */
//@DataJpaTest
//@ActiveProfiles("test")
//@DisplayName("StatementRepository Integration Tests")
//class StatementRepositoryTest {
//
//    @Autowired
//    private StatementRepository statementRepository;
//
//    private final String ACCOUNT_ID = "ACC_TEST";
//
//    @BeforeEach
//    void setUp() {
//        statementRepository.deleteAll();
//
//        statementRepository.saveAll(List.of(
//                buildStatement("ACC_TEST", LocalDate.of(2025, 1, 10), "50000", TransactionType.CREDIT, "RTEST001"),
//                buildStatement("ACC_TEST", LocalDate.of(2025, 2, 15), "3000",  TransactionType.DEBIT,  "RTEST002"),
//                buildStatement("ACC_TEST", LocalDate.of(2025, 3, 20), "2000",  TransactionType.DEBIT,  "RTEST003"),
//                buildStatement("ACC_OTHER", LocalDate.of(2025, 1, 5), "99999", TransactionType.CREDIT, "RTEST004")
//        ));
//    }
//
//    @Test
//    @DisplayName("Should return only statements matching accountId and date range")
//    void shouldReturnStatementsForAccountAndDateRange() {
//        Page<Statement> result = statementRepository.findByAccountIdAndDateRange(
//                ACCOUNT_ID,
//                LocalDate.of(2025, 1, 1),
//                LocalDate.of(2025, 3, 31),
//                PageRequest.of(0, 10)
//        );
//
//        assertThat(result.getContent()).hasSize(3);
//        assertThat(result.getContent())
//                .extracting(Statement::getAccountId)
//                .containsOnly(ACCOUNT_ID);
//    }
//
//    @Test
//    @DisplayName("Should exclude statements outside the date range")
//    void shouldExcludeStatementsOutsideDateRange() {
//        Page<Statement> result = statementRepository.findByAccountIdAndDateRange(
//                ACCOUNT_ID,
//                LocalDate.of(2025, 1, 1),
//                LocalDate.of(2025, 1, 31),
//                PageRequest.of(0, 10)
//        );
//
//        assertThat(result.getContent()).hasSize(1);
//        assertThat(result.getContent().get(0).getTransactionDate()).isEqualTo(LocalDate.of(2025, 1, 10));
//    }
//
//    @Test
//    @DisplayName("Should return empty page when no statements match")
//    void shouldReturnEmptyPageWhenNoMatch() {
//        Page<Statement> result = statementRepository.findByAccountIdAndDateRange(
//                "UNKNOWN_ACCOUNT",
//                LocalDate.of(2025, 1, 1),
//                LocalDate.of(2025, 12, 31),
//                PageRequest.of(0, 10)
//        );
//
//        assertThat(result.getContent()).isEmpty();
//        assertThat(result.getTotalElements()).isZero();
//    }
//
//    @Test
//    @DisplayName("Should return correct debit/credit summary")
//    void shouldReturnCorrectDebitCreditSummary() {
//        List<Object[]> summary = statementRepository.getDebitCreditSummary(
//                ACCOUNT_ID,
//                LocalDate.of(2025, 1, 1),
//                LocalDate.of(2025, 3, 31)
//        );
//
//        assertThat(summary).isNotEmpty();
//        Object[] row = summary.get(0);
//        BigDecimal credits = (BigDecimal) row[0];
//        BigDecimal debits  = (BigDecimal) row[1];
//
//        assertThat(credits).isEqualByComparingTo("50000");
//        assertThat(debits).isEqualByComparingTo("5000");
//    }
//
//    @Test
//    @DisplayName("Pagination should work correctly")
//    void shouldPaginateCorrectly() {
//        Page<Statement> page1 = statementRepository.findByAccountIdAndDateRange(
//                ACCOUNT_ID,
//                LocalDate.of(2025, 1, 1),
//                LocalDate.of(2025, 12, 31),
//                PageRequest.of(0, 2)
//        );
//        Page<Statement> page2 = statementRepository.findByAccountIdAndDateRange(
//                ACCOUNT_ID,
//                LocalDate.of(2025, 1, 1),
//                LocalDate.of(2025, 12, 31),
//                PageRequest.of(1, 2)
//        );
//
//        assertThat(page1.getContent()).hasSize(2);
//        assertThat(page2.getContent()).hasSize(1);
//        assertThat(page1.getTotalElements()).isEqualTo(3L);
//        assertThat(page1.getTotalPages()).isEqualTo(2);
//        assertThat(page1.isFirst()).isTrue();
//        assertThat(page2.isLast()).isTrue();
//    }
//
//    // ------------------------------------------------------------------ //
//    //  Helper                                                               //
//    // ------------------------------------------------------------------ //
//
//    private Statement buildStatement(String accountId, LocalDate date,
//                                     String amount, TransactionType type, String ref) {
//        return Statement.builder()
//                .accountId(accountId)
//                .transactionDate(date)
//                .amount(new BigDecimal(amount))
//                .type(type)
//                .description("Test transaction")
//                .balance(new BigDecimal("100000"))
//                .referenceNumber(ref)
//                .currency("INR")
//                .build();
//    }
//}
