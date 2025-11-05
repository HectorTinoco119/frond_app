package com.example.sistema_procesos.Auth;

import com.example.sistema_procesos.Jwt.JwtService;
import com.example.sistema_procesos.User.User;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request, HttpServletResponse res) {
        AuthResponse r = authService.register(request);
        setRateLimitHeaders(res);
        return ResponseEntity.status(201).body(r);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpServletResponse res) {
        AuthResponse r = authService.login(request);
        setRateLimitHeaders(res);
        return ResponseEntity.ok(r);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token no válido o ausente"));
        }
        authService.logout(authHeader);
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada"));
    }

    @GetMapping("/profile")
    public ResponseEntity<User> profile(@RequestHeader("Authorization") String authHeader) {
        User user = authService.getUserFromToken(authHeader);
        return ResponseEntity.ok(user);
    }

    // Rate limit headers
    private static final int MAX_REQUESTS_PER_HOUR = 600;
    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final long ONE_HOUR_MILLIS = 60 * 60 * 1000L;
    private static final long ONE_MINUTE_MILLIS = 60 * 1000L;

    private void setRateLimitHeaders(HttpServletResponse res) {
        // For demo: compute simple remaining values. Replace with real counters if needed.
        int currentHourCount = 0;
        int currentMinuteCount = 0;
        res.setHeader("X-RateLimit-Limit-Hour", String.valueOf(MAX_REQUESTS_PER_HOUR));
        res.setHeader("X-RateLimit-Remaining-Hour", String.valueOf(Math.max(0, MAX_REQUESTS_PER_HOUR - currentHourCount)));
        res.setHeader("X-RateLimit-Limit-Minute", String.valueOf(MAX_REQUESTS_PER_MINUTE));
        res.setHeader("X-RateLimit-Remaining-Minute", String.valueOf(Math.max(0, MAX_REQUESTS_PER_MINUTE - currentMinuteCount)));
        res.setHeader("X-RateLimit-Reset-Hour", String.valueOf(ONE_HOUR_MILLIS / 1000)); // seconds
        res.setHeader("X-RateLimit-Reset-Minute", String.valueOf(ONE_MINUTE_MILLIS / 1000)); // seconds
    }
}
