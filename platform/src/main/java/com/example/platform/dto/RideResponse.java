package com.example.platform.dto;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class RideResponse {
    private String rideId;
    private boolean result; //true, false
    private String driveId;
    private Integer ETA; //예상 시간
    private Timestamp timestamp;
}
