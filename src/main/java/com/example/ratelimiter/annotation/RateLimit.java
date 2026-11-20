package com.example.ratelimiter.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** Máximo de requisições permitidas por janela. */
    int requests() default 5;

    /** Duração da janela, em segundos. */
    int seconds() default 10;

    /** Critério usado para identificar o cliente. */
    KeyType keyType() default KeyType.IP;
}
