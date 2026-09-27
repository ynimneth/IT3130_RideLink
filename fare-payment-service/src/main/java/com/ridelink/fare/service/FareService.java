package com.ridelink.fare.service;

import com.ridelink.fare.dto.FareEstimateRequest;
import com.ridelink.fare.model.FareEstimate;
import com.ridelink.fare.repository.FareEstimateRepository;
import org.springframework.stereotype.Service;

@Service
public class FareService {

    private final FareEstimateRepository fareEstimateRepository;

    private static final double BASE_FARE = 100.0;
    private static final double MINIMUM_FARE = 200.0;

    public FareService(FareEstimateRepository fareEstimateRepository) {
        this.fareEstimateRepository = fareEstimateRepository;
    }

    public FareEstimate estimateFare(FareEstimateRequest request) {
        double perKmRate = getPerKmRate(request.getVehicleType());
        double estimatedTotal = Math.max(BASE_FARE + (perKmRate * request.getDistanceKm()), MINIMUM_FARE);

        FareEstimate estimate = new FareEstimate(
                request.getRideId(),
                BASE_FARE,
                perKmRate,
                request.getDistanceKm(),
                estimatedTotal
        );

        return fareEstimateRepository.save(estimate);
    }

    public FareEstimate getFareEstimateByRideId(String rideId) {
        return fareEstimateRepository.findByRideId(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Fare estimate not found for ride ID: " + rideId));
    }

    private double getPerKmRate(String vehicleType) {
        if (vehicleType == null) return 50.0;
        return switch (vehicleType.toUpperCase()) {
            case "SUV" -> 65.0;
            case "THREE_WHEELER" -> 40.0;
            default -> 50.0; // SEDAN
        };
    }
}
