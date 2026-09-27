package com.ridelink.fare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.fare.dto.FareEstimateRequest;
import com.ridelink.fare.model.FareEstimate;
import com.ridelink.fare.security.JwtUtil;
import com.ridelink.fare.service.FareService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FareControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FareService fareService;

    @MockBean
    private org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory;

    private String generateToken(String email, String role, String userId) {
        return io.jsonwebtoken.Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("userId", userId)
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + 86400000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                        "RideLinkSuperSecretKeyForJWTTokenGeneration2024MustBeLongEnough"
                                .getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();
    }

    @Test
    @DisplayName("POST /api/fares/estimate — returns fare estimate")
    void estimateFare_returnsCreated() throws Exception {
        FareEstimateRequest request = new FareEstimateRequest("ride-123", 10.0, "SEDAN");
        FareEstimate estimate = new FareEstimate("ride-123", 100.0, 50.0, 10.0, 600.0);

        when(fareService.estimateFare(any(FareEstimateRequest.class))).thenReturn(estimate);

        mockMvc.perform(post("/api/fares/estimate")
                        .header("Authorization", "Bearer " + generateToken("user@example.com", "PASSENGER", "user-123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estimatedTotal").value(600.0));
    }
}
