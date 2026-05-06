//package com.tarique.bankstatement.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.tarique.bankstatement.dto.response.PagedResponse;
//import com.tarique.bankstatement.dto.response.StatementResponse;
//import com.tarique.bankstatement.entity.enums.TransactionType;
//import com.tarique.bankstatement.repository.UserRepository;
//import com.tarique.bankstatement.security.jwt.JwtAuthEntryPoint;
//import com.tarique.bankstatement.security.jwt.JwtProvider;
//import com.tarique.bankstatement.security.service.UserDetailsServiceImpl;
//import com.tarique.bankstatement.service.StatementService;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//
//import com.tarique.bankstatement.config.SecurityConfig;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.List;
//import java.util.UUID;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
///**
// * Controller-layer tests using MockMvc (Spring slice test).
// *
// * <p>@WebMvcTest loads only the web layer. SecurityConfig is @Import-ed explicitly
// * so the JWT filter chain is active — this tests real security behaviour.
// * All service/repository dependencies are @MockBean-ed.
// */
//@WebMvcTest(StatementController.class)
//@Import(SecurityConfig.class)
//@DisplayName("StatementController Web Layer Tests")
//class StatementControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    // ---- Service mock ----
//    @MockBean
//    private StatementService statementService;
//
//    // ---- SecurityConfig dependencies (must mock all for context to start) ----
//    @MockBean
//    private JwtProvider jwtProvider;
//
//    @MockBean
//    private JwtAuthEntryPoint jwtAuthEntryPoint;
//
//    @MockBean
//    private UserDetailsServiceImpl userDetailsService;
//
//    @MockBean
//    private UserRepository userRepository;
//
//    @MockBean
//    private ObjectMapper objectMapperMock;
//
//    private static final String BASE_URL = "/api/v1/statements";
//
//    // ------------------------------------------------------------------ //
//    //  Happy Path                                                           //
//    // ------------------------------------------------------------------ //
//
//    @Test
//    @WithMockUser(username = "testuser", roles = "USER")
//    @DisplayName("GET /statements/{accountId} returns 200 with paginated data")
//    void shouldReturn200WithStatements() throws Exception {
//        StatementResponse stmt = StatementResponse.builder()
//                .id(UUID.randomUUID())
//                .accountId("ACC001")
//                .transactionDate(LocalDate.of(2025, 1, 5))
//                .amount(new BigDecimal("50000.00"))
//                .type(TransactionType.CREDIT)
//                .description("Salary")
//                .currency("INR")
//                .build();
//
//        PagedResponse<StatementResponse> pagedResponse = PagedResponse.<StatementResponse>builder()
//                .content(List.of(stmt))
//                .page(1).size(20)
//                .totalElements(1L).totalPages(1)
//                .first(true).last(true)
//                .summary(PagedResponse.SummaryStats.builder()
//                        .totalCredits(new BigDecimal("50000"))
//                        .totalDebits(BigDecimal.ZERO)
//                        .netBalance(new BigDecimal("50000"))
//                        .transactionCount(1L)
//                        .build())
//                .build();
//
//        when(statementService.getStatements(
//                eq("ACC001"), any(LocalDate.class), any(LocalDate.class),
//                anyInt(), anyInt(), anyString(), anyString()))
//                .thenReturn(pagedResponse);
//
//        mockMvc.perform(get(BASE_URL + "/ACC001")
//                        .param("from", "2025-01-01")
//                        .param("to",   "2025-03-31")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data.content").isArray())
//                .andExpect(jsonPath("$.data.content[0].accountId").value("ACC001"))
//                .andExpect(jsonPath("$.data.content[0].type").value("CREDIT"))
//                .andExpect(jsonPath("$.data.summary.totalCredits").value(50000))
//                .andExpect(jsonPath("$.data.totalElements").value(1));
//    }
//
//    @Test
//    @WithMockUser(username = "admin", roles = "ADMIN")
//    @DisplayName("ADMIN role can fetch statements")
//    void shouldReturn200ForAdminRole() throws Exception {
//        when(statementService.getStatements(any(), any(), any(), anyInt(), anyInt(), any(), any()))
//                .thenReturn(PagedResponse.<StatementResponse>builder()
//                        .content(List.of())
//                        .totalElements(0L).totalPages(0)
//                        .build());
//
//        mockMvc.perform(get(BASE_URL + "/ACC999")
//                        .param("from", "2025-01-01")
//                        .param("to",   "2025-01-31")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true));
//    }
//
//    // ------------------------------------------------------------------ //
//    //  Security                                                             //
//    // ------------------------------------------------------------------ //
//
//    @Test
//    @DisplayName("Returns 401 when no authentication provided")
//    void shouldReturn401WhenUnauthenticated() throws Exception {
//        mockMvc.perform(get(BASE_URL + "/ACC001")
//                        .param("from", "2025-01-01")
//                        .param("to",   "2025-03-31"))
//                .andExpect(status().isUnauthorized());
//    }
//
//    // ------------------------------------------------------------------ //
//    //  Validation                                                           //
//    // ------------------------------------------------------------------ //
//
//    @Test
//    @WithMockUser(roles = "USER")
//    @DisplayName("Returns 400 when 'from' parameter is missing")
//    void shouldReturn400WhenFromMissing() throws Exception {
//        mockMvc.perform(get(BASE_URL + "/ACC001")
//                        .param("to", "2025-03-31")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    @WithMockUser(roles = "USER")
//    @DisplayName("Returns 400 when 'to' parameter is missing")
//    void shouldReturn400WhenToMissing() throws Exception {
//        mockMvc.perform(get(BASE_URL + "/ACC001")
//                        .param("from", "2025-01-01")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest());
//    }
//}
