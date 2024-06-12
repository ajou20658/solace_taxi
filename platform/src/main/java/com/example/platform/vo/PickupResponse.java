package com.example.platform.vo;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class PickupResponse {
    private String rideId;
    private String driveId;
    private Integer ETA;
    private boolean result; //0 거부 1 수락
    private Timestamp timestamp;
}
