package com.example.ratelimiter.annotation;

public enum KeyType {
    /** Limita por endereço IP do cliente. */
    IP,
    /** Limita pelo usuário (header X-User-Id); sem o header, usa o IP. */
    USER
}
