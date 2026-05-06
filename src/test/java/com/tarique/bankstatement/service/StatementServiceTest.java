//package com.tarique.bankstatement.service;
//
//import com.tarique.bankstatement.dto.response.PagedResponse;
//import com.tarique.bankstatement.dto.response.StatementResponse;
//import com.tarique.bankstatement.entity.Statement;
//import com.tarique.bankstatement.entity.enums.TransactionType;
//import com.tarique.bankstatement.repository.StatementRepository;
//import com.tarique.bankstatement.service.impl.StatementServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.*;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.List;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
///**
// * Unit tests for StatementServiceImpl.
// * Uses Mockito — no Spring context, no DB, runs in milliseconds.
// */
//@ExtendWith(MockitoExtension.class)
//@DisplayName("StatementService Unit Tests")
//class StatementServiceTest {
//
//    @Mock
//    private StatementRepository statementRepository;
//
//    @InjectMocks
//    private StatementServiceImpl statementService;
//
//    private Statement sampleStatement;
//    private final String ACCOUNT_ID = "ACC001";
//    private final LocalDate FROM    = LocalDate.of(2025, 1, 1);
//    private final LocalDate TO      = LocalDate.of(2025, 3, 31);
//
//    @BeforeEach
//    void setUp() {
//        sampleStatement = Statement.builder()
//                .id(UUID.randomUUID())
//                .accountId(ACCOUNT_ID)
//                .transactionDate(LocalDate.of(2025, 1, 5))
//                .amount(new BigDecimal("50000.00"))
//                .type(TransactionType.CREDIT)
//                .description("Salary")
//                .balance(new BigDecimal("150000.00"))
//                .referenceNumber("TXN001")
//                .currency("INR")
//                .build();
//    }
//
//    // ------------------------------------------------------------------ //
//    //  Happy Path                                                           //
//    // ------------------------------------------------------------------ //
//
//    @Nested
//    @DisplayName("Happy Path Tests")
//    class HappyPath {
//
//        @Test
//        @DisplayName("Should return paginated statements for valid inputs")
//        void shouldReturnStatementsForValidInputs() {
//            // Arrange
//            Page<Statement> page = new PageImpl<>(
//                    List.of(sampleStatement),
//                    PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "transactionDate")),
//                    1L
//            );
//            when(statementRepository.findByAccountIdAndDateRange(
//                    eq(ACCOUNT_ID), eq(FROM), eq(TO), any(Pageable.class)))
//                    .thenReturn(page);
// //           when(statementRepository.getDebitCreditSummary(eq(ACCOUNT_ID), eq(FROM), eq(TO)))
////                    .thenReturn(List.of(new Object[]{new BigDecimal("50000"), new BigDecimal("10000")}));
//
//            // Act
//            PagedResponse<StatementResponse> result =
//                    statementService.getStatements(ACCOUNT_ID, FROM, TO, 1, 20, "transactionDate", "desc");
//
//            // Assert
//            assertThat(result).isNotNull();
//            assertThat(result.getContent()).hasSize(1);
//            assertThat(result.getTotalElements()).isEqualTo(1L);
//            assertThat(result.getPage()).isEqualTo(1);
//         //   assertThat(result.getSummary().getTotalCredits()).isEqualByComparingTo("50000");
//            assertThat(result.getSummary().getTotalDebits()).isEqualByComparingTo("10000");
//            assertThat(result.getSummary().getNetBalance()).isEqualByComparingTo("40000");
//        }
//
//        @Test
//        @DisplayName("Should return empty content when no statements found")
//        void shouldReturnEmptyContentWhenNoStatements() {
//            Page<Statement> emptyPage = Page.empty(PageRequest.of(0, 20));
//            when(statementRepository.findByAccountIdAndDateRange(any(), any(), any(), any()))
//                    .thenReturn(emptyPage);
////            when(statementRepository.getDebitCreditSummary(any(), any(), any()))
////                    .thenReturn(List.of(new Object[]{null, null}));
//
//            PagedResponse<StatementResponse> result =
//                    statementService.getStatements(ACCOUNT_ID, FROM, TO, 1, 20, "transactionDate", "desc");
//
//            assertThat(result.getContent()).isEmpty();
//            assertThat(result.getTotalElements()).isZero();
//            assertThat(result.getSummary().getTotalCredits()).isEqualByComparingTo(BigDecimal.ZERO);
//        }
//
//        @Test
//        @DisplayName("Should cap page size at MAX_PAGE_SIZE (500)")
//        void shouldCapPageSizeAt500() {
//            Page<Statement> page = new PageImpl<>(List.of(sampleStatement));
//            when(statementRepository.findByAccountIdAndDateRange(any(), any(), any(), any()))
//                    .thenReturn(page);
//            when(statementRepository.getDebitCreditSummary(any(), any(), any()))
//                    .thenReturn(List.of());
//
//            PagedResponse<StatementResponse> result =
//                    statementService.getStatements(ACCOUNT_ID, FROM, TO, 1, 9999, "transactionDate", "desc");
//
//            assertThat(result.getSize()).isEqualTo(500);
//        }
//
//        @Test
//        @DisplayName("Should sort by amount when sortBy=amount")
//        void shouldSortByAmount() {
//            Page<Statement> page = new PageImpl<>(List.of(sampleStatement));
//            when(statementRepository.findByAccountIdAndDateRange(
//                    eq(ACCOUNT_ID), eq(FROM), eq(TO),
//                    argThat(p -> p.getSort().getOrderFor("amount") != null)))
//                    .thenReturn(page);
//            when(statementRepository.getDebitCreditSummary(any(), any(), any()))
//                    .thenReturn(List.of());
//
//            PagedResponse<StatementResponse> result =
//                    statementService.getStatements(ACCOUNT_ID, FROM, TO, 1, 20, "amount", "asc");
//
//            assertThat(result).isNotNull();
//            verify(statementRepository).findByAccountIdAndDateRange(
//                    eq(ACCOUNT_ID), eq(FROM), eq(TO),
//                    argThat(p -> p.getSort().getOrderFor("amount") != null));
//        }
//    }
//
//    // ------------------------------------------------------------------ //
//    //  Validation / Edge Cases                                              //
//    // ------------------------------------------------------------------ //
//
//    @Nested
//    @DisplayName("Validation Tests")
//    class Validation {
//
//        @Test
//        @DisplayName("Should throw exception when 'from' is after 'to'")
//        void shouldThrowWhenFromAfterTo() {
//            assertThatThrownBy(() ->
//                    statementService.getStatements(ACCOUNT_ID,
//                            LocalDate.of(2025, 3, 1), LocalDate.of(2025, 1, 1),
//                            1, 20, "transactionDate", "desc"))
//                    .isInstanceOf(IllegalArgumentException.class)
//                    .hasMessageContaining("'from' date must not be after 'to' date");
//        }
//
//        @Test
//        @DisplayName("Should throw exception when date range exceeds 365 days")
//        void shouldThrowWhenDateRangeExceeds365Days() {
//            assertThatThrownBy(() ->
//                    statementService.getStatements(ACCOUNT_ID,
//                            LocalDate.of(2024, 1, 1), LocalDate.of(2025, 6, 1),
//                            1, 20, "transactionDate", "desc"))
//                    .isInstanceOf(IllegalArgumentException.class)
//                    .hasMessageContaining("Date range must not exceed 365 days");
//        }
//
//        @Test
//        @DisplayName("Should throw exception when 'from' is null")
//        void shouldThrowWhenFromIsNull() {
//            assertThatThrownBy(() ->
//                    statementService.getStatements(ACCOUNT_ID, null, TO, 1, 20, "transactionDate", "desc"))
//                    .isInstanceOf(IllegalArgumentException.class)
//                    .hasMessageContaining("required");
//        }
//
//        @Test
//        @DisplayName("Should convert page=0 to pageIndex=0 without error")
//        void shouldHandlePageZeroGracefully() {
//            Page<Statement> page = new PageImpl<>(List.of(sampleStatement));
//            when(statementRepository.findByAccountIdAndDateRange(any(), any(), any(), any()))
//                    .thenReturn(page);
//            when(statementRepository.getDebitCreditSummary(any(), any(), any()))
//                    .thenReturn(List.of());
//
//            // page=0 from client → internally treated as page 0 (not negative)
//            assertThatNoException().isThrownBy(() ->
//                    statementService.getStatements(ACCOUNT_ID, FROM, TO, 0, 20, "transactionDate", "desc"));
//        }
//    }
//}
