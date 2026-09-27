package com.ridelink.fare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.fare.dto.ProcessPaymentRequest;
import com.ridelink.fare.model.Payment;
import com.ridelink.fare.model.PaymentMethod;
import com.ridelink.fare.model.PaymentStatus;
import com.ridelink.fare.security.JwtUtil;
import com.ridelink.fare.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

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
    @DisplayName("POST /api/payments/{rideId}/process — completes payment")
    void processPayment_returnsOk() throws Exception {
        ProcessPaymentRequest request = new ProcessPaymentRequest(PaymentMethod.CARD_SIMULATED);
        Payment payment = new Payment("ride-123", "user-123", "driver-456", 600.0, PaymentStatus.COMPLETED);
        payment.setPaymentMethod(PaymentMethod.CARD_SIMULATED);

        when(paymentService.processPayment(eq("ride-123"), eq("user-123"), any(ProcessPaymentRequest.class)))
                .thenReturn(payment);

        mockMvc.perform(post("/api/payments/ride-123/process")
                        .header("Authorization", "Bearer " + generateToken("user@example.com", "PASSENGER", "user-123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
