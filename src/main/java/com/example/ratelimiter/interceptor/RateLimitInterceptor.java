package com.example.ratelimiter.interceptor;

import java.time.Duration;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.ratelimiter.annotation.KeyType;
import com.example.ratelimiter.annotation.RateLimit;
import com.example.ratelimiter.exception.RateLimitExceededException;
import com.example.ratelimiter.service.RateLimitResult;
import com.example.ratelimiter.service.RateLimiterService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final String USER_HEADER = "X-User-Id";
    private static final String REMAINING_HEADER = "X-RateLimit-Remaining";

    private final RateLimiterService rateLimiter;

    public RateLimitInterceptor(RateLimiterService rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RateLimit rateLimit = findRateLimit(handlerMethod);
        if (rateLimit == null) {
            return true;
        }

        String key = handlerMethod.getShortLogMessage() + "|" + identifyClient(rateLimit, request);
        RateLimitResult result = rateLimiter.tryAcquire(key, rateLimit.requests(), Duration.ofSeconds(rateLimit.seconds()));

        if (!result.allowed()) {
            throw new RateLimitExceededException(result.retryAfterSeconds());
        }

        response.setHeader(REMAINING_HEADER, String.valueOf(result.remaining()));
        return true;
    }

    private RateLimit findRateLimit(HandlerMethod handlerMethod) {
        RateLimit onMethod = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), RateLimit.class);
        if (onMethod != null) {
            return onMethod;
        }
        return AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), RateLimit.class);
    }

    private String identifyClient(RateLimit rateLimit, HttpServletRequest request) {
        if (rateLimit.keyType() == KeyType.USER) {
            String user = request.getHeader(USER_HEADER);
            if (user != null && !user.isBlank()) {
                return "user:" + user;
            }
        }
        return "ip:" + request.getRemoteAddr();
    }
}
