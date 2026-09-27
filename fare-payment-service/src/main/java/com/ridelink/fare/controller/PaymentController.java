package com.ridelink.fare.controller;

import com.ridelink.fare.dto.ProcessPaymentRequest;
import com.ridelink.fare.model.Payment;
import com.ridelink.fare.security.JwtAuthenticationDetails;
import com.ridelink.fare.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment Management", description = "Payment processing and history endpoints")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{rideId}/process")
    @Operation(summary = "Process (simulate) payment for a ride")
    public ResponseEntity<Payment> processPayment(
            Authentication authentication,
            @PathVariable String rideId,
            @Valid @RequestBody ProcessPaymentRequest request) {
        String userId = getDetails(authentication).getUserId();
        Payment payment = paymentService.processPayment(rideId, userId, request);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/{rideId}")
    @Operation(summary = "Get payment for a ride")
    public ResponseEntity<Payment> getPaymentByRideId(@PathVariable String rideId) {
        Payment payment = paymentService.getPaymentByRideId(rideId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/history")
    @Operation(summary = "Get payment history for user")
    public ResponseEntity<List<Payment>> getPaymentHistory(Authentication authentication) {
        String userId = getDetails(authentication).getUserId();
        List<Payment> payments = paymentService.getPaymentHistory(userId);
        return ResponseEntity.ok(payments);
    }

    private JwtAuthenticationDetails getDetails(Authentication authentication) {
        return (JwtAuthenticationDetails) authentication.getDetails();
    }
}
