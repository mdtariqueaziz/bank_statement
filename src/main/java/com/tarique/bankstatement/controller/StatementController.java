package com.tarique.bankstatement.controller;

import com.tarique.bankstatement.dto.response.ApiResponse;
import com.tarique.bankstatement.dto.response.PagedResponse;
import com.tarique.bankstatement.dto.response.StatementResponse;
import com.tarique.bankstatement.service.StatementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * StatementController — primary endpoint for the challenge.
 *
 * <pre>GET /api/v1/statements/{accountId}?from=&amp;to=&amp;page=&amp;size=&amp;sortBy=&amp;sortDir=</pre>
 *
 * <p>Annotated with Swagger/OpenAPI 3 docs for visibility.
 * Mirrors the reference my-project controller pattern
 * (@Api, @ApiOperation → @Tag, @Operation in OpenAPI 3).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/statements")
@RequiredArgsConstructor
@Tag(name = "Bank Statements", description = "Retrieve paginated bank statements by account and date range")
@SecurityRequirement(name = "bearerAuth")
public class StatementController {

    private final StatementService statementService;

    /**
     * Primary Challenge Endpoint:
     * GET /api/v1/statements/{accountId}?from={date}&to={date}
     */
    @GetMapping("/{accountId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
        summary     = "Get statements for an account",
        description = "Returns paginated bank statements for the given account within the specified date range. " +
                      "Designed for 10K req/sec across 50M accounts with Caffeine caching and index-optimised queries."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description  = "Statements fetched successfully",
            content      = @Content(schema = @Schema(implementation = PagedResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid date range or parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized — JWT missing or expired"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Account not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Database unavailable")
    })
    public ResponseEntity<ApiResponse<PagedResponse<StatementResponse>>> getStatements(

            @Parameter(description = "Bank account identifier", required = true, in = ParameterIn.PATH)
            @PathVariable String accountId,

            @Parameter(description = "Start date (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "End date (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @Parameter(description = "Page number (1-based)", example = "1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "Records per page (max 500)", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Sort field: transactionDate | amount | type", example = "transactionDate")
            @RequestParam(defaultValue = "transactionDate") String sortBy,

            @Parameter(description = "Sort direction: asc | desc", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        log.info("GET /api/v1/statements/{} from={} to={} page={} size={}", accountId, from, to, page, size);

        PagedResponse<StatementResponse> result =
                statementService.getStatements(accountId, from, to, page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
