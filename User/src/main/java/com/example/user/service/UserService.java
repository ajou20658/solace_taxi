package com.example.user.service;

import com.example.user.vo.PaymentRequest;
import com.example.user.vo.RideRequest;
import com.example.user.vo.RideResponse;
import com.example.user.vo.UserRideRequest;
import com.solacesystems.jcsmp.JCSMPException;

public interface UserService {
    void rideRequest() throws JCSMPException;
    void handleRideResponse(RideResponse rideResponse);
    void handlePaymentRequest(PaymentRequest paymentRequest);
}
