package com.example.user.vo;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class PaymentRequest {
    private Integer cost;
    private String serviceType;
    private String rideId;
    private Timestamp timestamp;

}
