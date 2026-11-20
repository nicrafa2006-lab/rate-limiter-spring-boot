package com.example.ratelimiter.service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Rate limiter em memória com janela fixa. Cada chave tem um contador que
 * reinicia quando a janela expira.
 */
@Service
public class RateLimiterService {

    private record Window(long expiresAtMillis, int count) {
    }

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimitResult tryAcquire(String key, int limit, Duration duration) {
        long now = System.currentTimeMillis();

        Window window = windows.compute(key, (k, current) -> {
            if (current == null || current.expiresAtMillis() <= now) {
                return new Window(now + duration.toMillis(), 1);
            }
            return new Window(current.expiresAtMillis(), Math.min(current.count() + 1, limit + 1));
        });

        boolean allowed = window.count() <= limit;
        int remaining = Math.max(0, limit - window.count());
        long retryAfterSeconds = Math.max(1, (window.expiresAtMillis() - now + 999) / 1000);

        return new RateLimitResult(allowed, remaining, retryAfterSeconds);
    }

    @Scheduled(fixedRate = 60_000)
    void evictExpiredWindows() {
        long now = System.currentTimeMillis();
        windows.values().removeIf(window -> window.expiresAtMillis() <= now);
    }
}
