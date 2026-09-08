package com.telereceipt.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthTokenService {

    private static final long SESSION_VALIDITY_SECONDS = 30L * 24 * 60 * 60; // 30 days persistent session
    private static final long OTP_VALIDITY_SECONDS = 5 * 60; // 5 minutes

    private record AuthSession(Long telegramUserId, Instant expiresAt) {}
    private record OtpSession(String otp, Instant expiresAt) {}

    private final Map<String, AuthSession> activeTokens = new ConcurrentHashMap<>();
    private final Map<Long, OtpSession> activeOtps = new ConcurrentHashMap<>();

    /**
     * Generate a new secure login token for a Telegram user (30 days).
     */
    public String generateToken(Long telegramUserId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plusSeconds(SESSION_VALIDITY_SECONDS);
        activeTokens.put(token, new AuthSession(telegramUserId, expiresAt));
        return token;
    }

    /**
     * Generate a 6-digit numeric OTP code valid for 5 minutes.
     */
    public String generateOtp(Long telegramUserId) {
        String otp = String.format("%06d", (int) (Math.random() * 1000000));
        Instant expiresAt = Instant.now().plusSeconds(OTP_VALIDITY_SECONDS);
        activeOtps.put(telegramUserId, new OtpSession(otp, expiresAt));
        return otp;
    }

    /**
     * Verify 6-digit OTP and generate a persistent session token on success.
     */
    public String verifyOtpAndCreateToken(Long telegramUserId, String code) {
        if (code == null || telegramUserId == null) return null;
        OtpSession session = activeOtps.get(telegramUserId);
        if (session == null) return null;

        if (Instant.now().isAfter(session.expiresAt())) {
            activeOtps.remove(telegramUserId);
            return null;
        }

        if (session.otp().equals(code.trim())) {
            activeOtps.remove(telegramUserId);
            return generateToken(telegramUserId);
        }
        return null;
    }

    /**
     * Validate a token and return the associated telegramUserId, or null if invalid/expired.
     */
    public Long validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        AuthSession session = activeTokens.get(token.trim());
        if (session == null) {
            return null;
        }

        if (Instant.now().isAfter(session.expiresAt())) {
            activeTokens.remove(token.trim());
            return null;
        }

        return session.telegramUserId();
    }
}
