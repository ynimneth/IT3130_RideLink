package com.ridelink.fare.controller;

import com.ridelink.fare.dto.FareEstimateRequest;
import com.ridelink.fare.model.FareEstimate;
import com.ridelink.fare.service.FareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fares")
@Tag(name = "Fare Management", description = "Fare estimation endpoints")
public class FareController {

    private final FareService fareService;

    public FareController(FareService fareService) {
        this.fareService = fareService;
    }

    @PostMapping("/estimate")
    @Operation(summary = "Calculate fare estimate")
    public ResponseEntity<FareEstimate> estimateFare(@Valid @RequestBody FareEstimateRequest request) {
        FareEstimate estimate = fareService.estimateFare(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(estimate);
    }

    @GetMapping("/{rideId}")
    @Operation(summary = "Get fare estimate for a ride")
    public ResponseEntity<FareEstimate> getFareEstimate(@PathVariable String rideId) {
        FareEstimate estimate = fareService.getFareEstimateByRideId(rideId);
        return ResponseEntity.ok(estimate);
    }
}
