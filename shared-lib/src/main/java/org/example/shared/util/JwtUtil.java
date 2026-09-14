package org.example.shared.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Date;

public class JwtUtil {

    private final String secret;
    private final long expirationTime;
    private final Algorithm algorithm;

    public JwtUtil(String secret, long expirationTime) {
        this.secret = secret;
        this.expirationTime = expirationTime;
        this.algorithm = Algorithm.HMAC256(secret);
    }

    public String generateToken(Long userId, String email) {
        return JWT.create()
                .withSubject(userId.toString())
                .withClaim("email", email)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
                .sign(algorithm);
    }

    public Long extractUserId(String token) throws JWTVerificationException {
        DecodedJWT decoded = JWT.require(algorithm)
                .build()
                .verify(token);
        return Long.parseLong(decoded.getSubject());
    }

    public String extractEmail(String token) throws JWTVerificationException {
        DecodedJWT decoded = JWT.require(algorithm)
                .build()
                .verify(token);
        return decoded.getClaim("email").asString();
    }

    public boolean isTokenValid(String token) {
        try {
            JWT.require(algorithm).build().verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }
}
