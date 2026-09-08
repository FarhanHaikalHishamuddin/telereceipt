package com.telereceipt.controller;

import com.telereceipt.model.UserProfile;
import com.telereceipt.repository.UserProfileRepository;
import com.telereceipt.service.AuthTokenService;
import com.telereceipt.service.TelegramService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserProfileRepository userProfileRepository;
    private final AuthTokenService authTokenService;
    private final TelegramService telegramService;

    public AuthController(
            UserProfileRepository userProfileRepository,
            AuthTokenService authTokenService,
            TelegramService telegramService) {
        this.userProfileRepository = userProfileRepository;
        this.authTokenService = authTokenService;
        this.telegramService = telegramService;
    }

    public record RequestOtpRequest(String username) {}
    public record VerifyOtpRequest(String username, String code) {}

    /**
     * Request a 6-digit OTP sent to the user's Telegram chat.
     */
    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(@RequestBody RequestOtpRequest request) {
        if (request.username() == null || request.username().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Please provide your Telegram username."));
        }

        String cleaned = request.username().trim().replace("@", "");
        Optional<UserProfile> userOpt = findUser(cleaned);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "User @" + cleaned + " not found. Please type /start in @DuitHilang_bot first."
            ));
        }

        UserProfile user = userOpt.get();
        String otp = authTokenService.generateOtp(user.getTelegramUserId());

        String name = user.getFirstName() != null ? user.getFirstName() : user.getUsername();
        String message = String.format("""
            🔐 <b>Telereceipt Web Login</b>

            Hi <b>%s</b>, your 6-digit verification code is:

            <code>%s</code>

            <i>This code is valid for 5 minutes. Do not share it with anyone.</i>
            """,
            name,
            otp
        );

        telegramService.sendMessage(user.getTelegramUserId(), message);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Verification code sent to your Telegram chat!"
        ));
    }

    /**
     * Verify the 6-digit OTP and return a persistent 30-day session token.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        if (request.username() == null || request.code() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and code are required."));
        }

        String cleaned = request.username().trim().replace("@", "");
        Optional<UserProfile> userOpt = findUser(cleaned);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
        }

        UserProfile user = userOpt.get();
        String token = authTokenService.verifyOtpAndCreateToken(user.getTelegramUserId(), request.code());

        if (token == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired verification code."));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "token", token,
                "userId", user.getTelegramUserId(),
                "username", user.getUsername() != null ? user.getUsername() : ""
        ));
    }

    private Optional<UserProfile> findUser(String cleaned) {
        Optional<UserProfile> userOpt = userProfileRepository.findByUsernameIgnoreCase(cleaned);
        if (userOpt.isEmpty()) {
            try {
                Long id = Long.parseLong(cleaned);
                userOpt = userProfileRepository.findById(id);
            } catch (NumberFormatException ignored) {}
        }
        return userOpt;
    }
}
