package com.example.platform.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LogRepository extends JpaRepository<ServiceLog,String> {
//    Optional<ServiceLog> findServiceLogByUserIdAndStatus(String userId);
//    Optional<ServiceLog> findFirstByUserIdOrderByTimestamp(String userId);
    Optional<ServiceLog> findTopByUserIdAndRideIdOrderByTimestampAsc(String userId,String rideId);
    Optional<ServiceLog> findTopByDriverId(String driverId);
    Optional<ServiceLog> findTopByDriverIdOrderByTimestampDesc(String driverId);
}
