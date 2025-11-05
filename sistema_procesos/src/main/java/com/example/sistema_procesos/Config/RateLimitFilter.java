package com.example.sistema_procesos.Config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter implements Filter {

    private static final int MAX_REQUESTS_PER_HOUR = 600;
    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final long ONE_HOUR_MILLIS = 60 * 60 * 1000L;
    private static final long ONE_MINUTE_MILLIS = 60 * 1000L;

    private final Map<String, ClientInfo> clientMap = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String ip = request.getRemoteAddr();
        String token = request.getHeader("Authorization");
        String key = (token != null && token.startsWith("Bearer ")) ? token.substring(7) : ip;

        long now = Instant.now().toEpochMilli();
        ClientInfo info = clientMap.computeIfAbsent(key, k -> new ClientInfo(now));

        synchronized (info) {
            if (now - info.hourWindowStart >= ONE_HOUR_MILLIS) {
                info.hourWindowStart = now;
                info.hourCount.set(0);
            }
            if (now - info.minuteWindowStart >= ONE_MINUTE_MILLIS) {
                info.minuteWindowStart = now;
                info.minuteCount.set(0);
            }

            int currentHourCount = info.hourCount.incrementAndGet();
            int currentMinuteCount = info.minuteCount.incrementAndGet();

            response.setHeader("X-RateLimit-Limit-Hour", String.valueOf(MAX_REQUESTS_PER_HOUR));
            response.setHeader("X-RateLimit-Remaining-Hour", String.valueOf(Math.max(0, MAX_REQUESTS_PER_HOUR - currentHourCount)));
            response.setHeader("X-RateLimit-Limit-Minute", String.valueOf(MAX_REQUESTS_PER_MINUTE));
            response.setHeader("X-RateLimit-Remaining-Minute", String.valueOf(Math.max(0, MAX_REQUESTS_PER_MINUTE - currentMinuteCount)));

            long resetHourSeconds = (info.hourWindowStart + ONE_HOUR_MILLIS - now) / 1000;
            long resetMinuteSeconds = (info.minuteWindowStart + ONE_MINUTE_MILLIS - now) / 1000;
            response.setHeader("X-RateLimit-Reset-Hour", String.valueOf(Math.max(0, resetHourSeconds)));
            response.setHeader("X-RateLimit-Reset-Minute", String.valueOf(Math.max(0, resetMinuteSeconds)));

            if (currentHourCount > MAX_REQUESTS_PER_HOUR || currentMinuteCount > MAX_REQUESTS_PER_MINUTE) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Demasiadas solicitudes\"}");
                return;
            }
        }

        chain.doFilter(req, res);
    }

    private static class ClientInfo {
        long hourWindowStart;
        long minuteWindowStart;
        AtomicInteger hourCount = new AtomicInteger(0);
        AtomicInteger minuteCount = new AtomicInteger(0);

        ClientInfo(long now) {
            this.hourWindowStart = now;
            this.minuteWindowStart = now;
        }
    }
}
