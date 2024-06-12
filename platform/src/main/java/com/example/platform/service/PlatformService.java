package com.example.platform.service;

import com.example.platform.vo.DropoffComplete;
import com.example.platform.vo.PickupComplete;
import com.example.platform.vo.PickupResponse;
import com.example.platform.vo.RideRequest;
import com.solacesystems.jcsmp.JCSMPException;

public interface PlatformService {
    void init();
    void RideRequest2PickupRequest(RideRequest rideRequest) throws JCSMPException;
    void sendRideResponse(PickupResponse pickupResponse) throws JCSMPException;
    void sendPaymentRequest(DropoffComplete dropoffComplete);
}
