package com.example.ratelimiter.exception;

public record ApiError(int status, String error, String message, long retryAfterSeconds) {
}
