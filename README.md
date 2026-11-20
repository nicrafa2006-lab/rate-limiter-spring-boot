# Rate Limiter (Spring Boot)

Proteção de endpoints contra excesso de requisições com a anotação `@RateLimit`.
Ao passar do limite, a API responde `429 Too Many Requests` com um JSON claro.

## Uso

```java
@RateLimit(requests = 5, seconds = 10)                       // 5 req a cada 10 s, por IP
@GetMapping("/recurso-protegido")
public Map<String, String> recurso() { ... }

@RateLimit(requests = 3, seconds = 10, keyType = KeyType.USER) // por usuário (header X-User-Id)
```

A anotação também pode ser colocada na classe do controller para valer em todos os endpoints dela.
O limite é contado por endpoint e por cliente.

## Estrutura

```
src/main/java/com/example/ratelimiter
├── RateLimiterApplication.java
├── annotation
│   ├── KeyType.java                 # IP ou USER
│   └── RateLimit.java               # anotação
├── config
│   └── WebConfig.java               # registra o interceptor
├── controller
│   └── ResourceController.java      # endpoints de exemplo
├── exception
│   ├── ApiError.java                # corpo JSON do erro
│   ├── GlobalExceptionHandler.java  # monta a resposta 429
│   └── RateLimitExceededException.java
├── interceptor
│   └── RateLimitInterceptor.java    # lê a anotação e aplica o limite
└── service
    ├── RateLimitResult.java
    └── RateLimiterService.java      # contador em memória (janela fixa)
src/test/java/.../RateLimitIntegrationTest.java
```

## Executar

Requer JDK 17+ e Maven.

```bash
mvn spring-boot:run
mvn test
```

## Testar

```bash
for i in $(seq 1 6); do curl -i http://localhost:8080/api/v1/recurso-protegido; done
```

A 6ª chamada retorna:

```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Limite de requisições excedido. Tente novamente em 9 segundo(s).",
  "retryAfterSeconds": 9
}
```

O header `Retry-After` também é enviado. Nas respostas de sucesso, `X-RateLimit-Remaining`
informa quantas requisições ainda restam na janela.

Por usuário:

```bash
curl -i -H "X-User-Id: ana" http://localhost:8080/api/v1/recurso-por-usuario
```

## Observações

- **Janela fixa em memória**: simples e sem dependências, mas o contador vale só para uma instância
  da aplicação. Com várias instâncias, troque o `RateLimiterService` por uma versão com Redis
  ou Bucket4j; o interceptor e a anotação continuam iguais.
- **IP atrás de proxy/load balancer**: o projeto usa `request.getRemoteAddr()`. Se houver proxy,
  configure `server.forward-headers-strategy=native` para o Spring resolver o IP real.
- **Usuário**: o header `X-User-Id` é só para demonstração e pode ser forjado. Em produção, use o
  usuário autenticado (por exemplo, `request.getUserPrincipal()`).
- Janelas expiradas são removidas a cada minuto para a memória não crescer indefinidamente.
