package com.example.payment.dto;

import com.example.payment.vo.ServiceType;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class PaymentRequest2User {
    private Integer cost;
    private String rideId;
    private ServiceType serviceType;
    private Timestamp timestamp;
}
