package com.example.ratelimiter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returns429AfterLimitIsExceeded() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/v1/recurso-protegido")).andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/v1/recurso-protegido"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"));
    }

    @Test
    void limitsEachUserSeparately() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/api/v1/recurso-por-usuario").header("X-User-Id", "ana"))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/v1/recurso-por-usuario").header("X-User-Id", "ana"))
                .andExpect(status().isTooManyRequests());
        mockMvc.perform(get("/api/v1/recurso-por-usuario").header("X-User-Id", "bruno"))
                .andExpect(status().isOk());
    }

    @Test
    void doesNotLimitEndpointsWithoutAnnotation() throws Exception {
        for (int i = 0; i < 20; i++) {
            mockMvc.perform(get("/api/v1/recurso-livre")).andExpect(status().isOk());
        }
    }
}
