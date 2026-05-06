package com.tarique.bankstatement.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarique.bankstatement.dto.response.ApiResponse;
import com.tarique.bankstatement.entity.User;
import com.tarique.bankstatement.repository.UserRepository;
import com.tarique.bankstatement.security.service.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * JWT Authentication Filter — executed once per request.
 *
 * <p>Validates the Bearer token, verifies it against the stored access_token
 * (so invalidation/logout is possible), then sets the SecurityContext.
 * Mirrors the reference my-project JwtAuthTokenFilter pattern.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthTokenFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        log.debug("JWT filter processing: {}", path);

        try {
            String jwt = extractBearerToken(request);

            if (StringUtils.isNotBlank(jwt)) {

                if (jwtProvider.validateToken(jwt)) {
                    String username = jwtProvider.getUsernameFromToken(jwt);

                    // Verify token matches what's stored — enables server-side logout
                    Optional<User> userOpt = userRepository.findByAccessToken(jwt);

                    if (userOpt.isPresent()) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.debug("Authenticated user '{}' for path '{}'", username, path);

                    } else {
                        // Token valid but not in DB — user logged out
                        writeError(response, HttpStatus.UNAUTHORIZED, "LOGGED_OUT");
                        return;
                    }

                } else {
                    writeError(response, HttpStatus.UNAUTHORIZED, "SESSION_EXPIRED");
                    return;
                }
            }

        } catch (ExpiredJwtException ex) {
            log.warn("Expired JWT for request {}: {}", path, ex.getMessage());
            writeError(response, HttpStatus.UNAUTHORIZED, "SESSION_EXPIRED");
            return;
        } catch (Exception ex) {
            log.error("Cannot set user authentication for {}: {}", path, ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                              //
    // ------------------------------------------------------------------ //

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<?> body = ApiResponse.error(message);
        response.getWriter().write(objectMapper.writeValueAsString(body));
        response.getWriter().flush();
    }
}
