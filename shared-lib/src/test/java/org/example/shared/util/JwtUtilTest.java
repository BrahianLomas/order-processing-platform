package org.example.shared.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET = "test-secret-key-min-256-bits-long-for-hs256-algorithm";
    private static final long ONE_DAY_MS = 86_400_000L;

    @Test
    void generateToken_thenIsTokenValid_returnsTrue() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, ONE_DAY_MS);

        String token = jwtUtil.generateToken(42L, "demo@test.com");

        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    void generateToken_thenExtractUserId_returnsSameId() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, ONE_DAY_MS);

        String token = jwtUtil.generateToken(42L, "demo@test.com");

        assertThat(jwtUtil.extractUserId(token)).isEqualTo(42L);
    }

    @Test
    void generateToken_thenExtractEmail_returnsSameEmail() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, ONE_DAY_MS);

        String token = jwtUtil.generateToken(42L, "demo@test.com");

        assertThat(jwtUtil.extractEmail(token)).isEqualTo("demo@test.com");
    }

    @Test
    void isTokenValid_withGarbageToken_returnsFalse() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, ONE_DAY_MS);

        assertThat(jwtUtil.isTokenValid("not-a-real-jwt")).isFalse();
    }

    @Test
    void isTokenValid_withAlreadyExpiredToken_returnsFalse() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, -1_000L);

        String expiredToken = jwtUtil.generateToken(42L, "demo@test.com");

        assertThat(jwtUtil.isTokenValid(expiredToken)).isFalse();
    }

    @Test
    void isTokenValid_withTokenSignedByDifferentSecret_returnsFalse() {
        JwtUtil signer = new JwtUtil(SECRET, ONE_DAY_MS);
        JwtUtil verifier = new JwtUtil("a-completely-different-secret-key-1234567890", ONE_DAY_MS);

        String token = signer.generateToken(42L, "demo@test.com");

        assertThat(verifier.isTokenValid(token)).isFalse();
    }
}
