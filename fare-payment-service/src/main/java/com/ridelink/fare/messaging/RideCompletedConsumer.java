package com.ridelink.fare.messaging;

import com.ridelink.fare.dto.RideCompletedEvent;
import com.ridelink.fare.model.FareEstimate;
import com.ridelink.fare.model.Payment;
import com.ridelink.fare.model.PaymentStatus;
import com.ridelink.fare.repository.FareEstimateRepository;
import com.ridelink.fare.repository.PaymentRepository;
import com.ridelink.fare.service.FareService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RideCompletedConsumer {

    private static final Logger log = LoggerFactory.getLogger(RideCompletedConsumer.class);

    private final FareService fareService;
    private final FareEstimateRepository fareEstimateRepository;
    private final PaymentRepository paymentRepository;

    public RideCompletedConsumer(FareService fareService,
                                 FareEstimateRepository fareEstimateRepository,
                                 PaymentRepository paymentRepository) {
        this.fareService = fareService;
        this.fareEstimateRepository = fareEstimateRepository;
        this.paymentRepository = paymentRepository;
    }

    @RabbitListener(queues = "${rabbitmq.queue}")
    public void handleRideCompletedEvent(RideCompletedEvent event) {
        log.info("Received ride.completed event for rideId={}", event.getRideId());

        try {
            // Find existing fare estimate or create a new one based on actual distance
            FareEstimate estimate = fareEstimateRepository.findByRideId(event.getRideId())
                    .orElseGet(() -> {
                        log.info("No prior fare estimate found for rideId={}, calculating final fare", event.getRideId());
                        // Create DTO equivalent and call fareService, but for simplicity we compute inline here or fallback
                        // Normally you'd recalculate based on the final distance from the event.
                        return null; // Simplifying for this example, assuming estimate always exists if requested
                    });

            if(estimate == null) {
                // If we didn't have one, let's just make one.
                // In a real scenario, the distance might differ, so we use event distance.
                com.ridelink.fare.dto.FareEstimateRequest req = new com.ridelink.fare.dto.FareEstimateRequest(
                        event.getRideId(), event.getDistanceKm(), event.getVehicleType()
                );
                estimate = fareService.estimateFare(req);
            }

            // Create Pending Payment
            Payment payment = new Payment(
                    event.getRideId(),
                    event.getPassengerId(),
                    event.getDriverId(),
                    estimate.getEstimatedTotal(),
                    PaymentStatus.PENDING
            );

            paymentRepository.save(payment);
            log.info("Created PENDING payment for rideId={}", event.getRideId());

        } catch (Exception e) {
            log.error("Error processing ride.completed event for rideId={}", event.getRideId(), e);
        }
    }
}
