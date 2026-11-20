package com.example.ratelimiter.service;

public record RateLimitResult(boolean allowed, int remaining, long retryAfterSeconds) {
}
