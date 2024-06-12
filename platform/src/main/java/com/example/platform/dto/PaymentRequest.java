package com.example.platform.dto;

import com.example.platform.vo.ServiceType;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class PaymentRequest {
    private String userId;
    private Integer cost;
    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    private ServiceType serviceType;
}
