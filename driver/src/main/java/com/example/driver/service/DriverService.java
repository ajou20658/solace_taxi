package com.example.driver.service;

import com.example.driver.dto.PickupRequest;

public interface DriverService {
    void pickupRequest2Response(PickupRequest pickupRequest);
    void sendLocationUpdateProducer();
    void pickupComplete();
    void dropoffComplete();
}
