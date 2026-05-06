//package com.tarique.bankstatement.security;
//
//import com.tarique.bankstatement.security.jwt.JwtProvider;
//import com.tarique.bankstatement.security.service.UserPrincipal;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import static org.assertj.core.api.Assertions.*;
//
///**
// * Unit tests for JwtProvider — no Spring context needed.
// */
//@DisplayName("JwtProvider Unit Tests")
//class JwtProviderTest {
//
//    private JwtProvider jwtProvider;
//
//    // Valid Base64-encoded 512-bit key for testing
//    private static final String TEST_SECRET =
//            "dGVzdFNlY3JldEtleUZvckJhbmtTdGF0ZW1lbnRTZXJ2aWNlV2l0aEVub3VnaEJpdHNGb3JITVNIT0xEQWxnb3JpdGht";
//    private static final long   TEST_EXPIRY = 3600000L; // 1 hour
//
//    @BeforeEach
//    void setUp() {
//        jwtProvider = new JwtProvider();
//        ReflectionTestUtils.setField(jwtProvider, "jwtSecret", TEST_SECRET);
//        ReflectionTestUtils.setField(jwtProvider, "jwtExpirationMs", TEST_EXPIRY);
//    }
//
//    private Authentication mockAuthentication(String username) {
//        UserPrincipal principal = new UserPrincipal(
//                1L, username, username + "@test.com", "USER", "encodedPass", true);
//        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
//    }
//
//    @Test
//    @DisplayName("Should generate a non-null JWT token")
//    void shouldGenerateToken() {
//        Authentication auth = mockAuthentication("testuser");
//        String token = jwtProvider.generateToken(auth);
//
//        assertThat(token).isNotNull().isNotEmpty();
//        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
//    }
//
//    @Test
//    @DisplayName("Should validate a freshly generated token")
//    void shouldValidateGeneratedToken() {
//        Authentication auth = mockAuthentication("testuser");
//        String token = jwtProvider.generateToken(auth);
//
//        assertThat(jwtProvider.validateToken(token)).isTrue();
//    }
//
//    @Test
//    @DisplayName("Should extract correct username from token")
//    void shouldExtractUsername() {
//        Authentication auth = mockAuthentication("john_doe");
//        String token = jwtProvider.generateToken(auth);
//
//        String username = jwtProvider.getUsernameFromToken(token);
//        assertThat(username).isEqualTo("john_doe");
//    }
//
//    @Test
//    @DisplayName("Should reject a tampered token")
//    void shouldRejectTamperedToken() {
//        Authentication auth = mockAuthentication("testuser");
//        String token = jwtProvider.generateToken(auth);
//
//        // Tamper with the signature part
//        String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "invalidSignature";
//
//        assertThat(jwtProvider.validateToken(tampered)).isFalse();
//    }
//
//    @Test
//    @DisplayName("Should reject a malformed token")
//    void shouldRejectMalformedToken() {
//        assertThat(jwtProvider.validateToken("not.a.jwt")).isFalse();
//    }
//
//    @Test
//    @DisplayName("Should reject empty or blank token")
//    void shouldRejectBlankToken() {
//        assertThat(jwtProvider.validateToken("")).isFalse();
//        assertThat(jwtProvider.validateToken("   ")).isFalse();
//    }
//
//    @Test
//    @DisplayName("Should generate different tokens for different users")
//    void shouldGenerateDifferentTokensForDifferentUsers() {
//        String token1 = jwtProvider.generateToken(mockAuthentication("userA"));
//        String token2 = jwtProvider.generateToken(mockAuthentication("userB"));
//
//        assertThat(token1).isNotEqualTo(token2);
//    }
//}
