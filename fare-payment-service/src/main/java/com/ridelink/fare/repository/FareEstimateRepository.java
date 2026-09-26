package com.ridelink.fare.repository;

import com.ridelink.fare.model.FareEstimate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FareEstimateRepository extends MongoRepository<FareEstimate, String> {

    Optional<FareEstimate> findByRideId(String rideId);
}
