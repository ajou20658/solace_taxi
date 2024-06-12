package com.example.driver.dto;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class PickupResponse {
    private String rideId;
    private String driveId;
    private Integer ETA;
    private boolean result;
    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());
}
