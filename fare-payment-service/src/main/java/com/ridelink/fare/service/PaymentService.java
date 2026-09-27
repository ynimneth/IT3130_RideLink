package com.ridelink.fare.service;

import com.ridelink.fare.dto.ProcessPaymentRequest;
import com.ridelink.fare.model.Payment;
import com.ridelink.fare.model.PaymentStatus;
import com.ridelink.fare.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment processPayment(String rideId, String userId, ProcessPaymentRequest request) {
        Payment payment = paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Payment record not found for ride ID: " + rideId));

        if (!payment.getPassengerId().equals(userId)) {
            throw new IllegalArgumentException("Only the passenger can process the payment");
        }

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Payment is already completed");
        }

        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setProcessedAt(Instant.now());

        return paymentRepository.save(payment);
    }

    public Payment getPaymentByRideId(String rideId) {
        return paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Payment record not found for ride ID: " + rideId));
    }

    public List<Payment> getPaymentHistory(String passengerId) {
        return paymentRepository.findByPassengerId(passengerId);
    }
}
