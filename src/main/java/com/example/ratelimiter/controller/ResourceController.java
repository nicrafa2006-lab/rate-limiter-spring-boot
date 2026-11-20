package com.example.ratelimiter.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ratelimiter.annotation.KeyType;
import com.example.ratelimiter.annotation.RateLimit;

@RestController
@RequestMapping("/api/v1")
public class ResourceController {

    @RateLimit(requests = 5, seconds = 10)
    @GetMapping("/recurso-protegido")
    public Map<String, String> protectedResource() {
        return Map.of("message", "Acesso permitido ao recurso protegido.");
    }

    @RateLimit(requests = 3, seconds = 10, keyType = KeyType.USER)
    @GetMapping("/recurso-por-usuario")
    public Map<String, String> perUserResource() {
        return Map.of("message", "Acesso permitido ao recurso limitado por usuário.");
    }

    @GetMapping("/recurso-livre")
    public Map<String, String> openResource() {
        return Map.of("message", "Recurso sem limite de requisições.");
    }
}
