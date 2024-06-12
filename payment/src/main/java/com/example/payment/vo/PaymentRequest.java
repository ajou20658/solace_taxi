package com.example.payment.vo;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class PaymentRequest {
    private String userId;
    private Integer cost;
    private Timestamp timestamp;
    private ServiceType serviceType;
}