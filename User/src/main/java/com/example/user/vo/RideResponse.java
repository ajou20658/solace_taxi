package com.example.user.vo;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class RideResponse {
    private String rideId;
    private String driveId;
    private Integer ETA; //예상 시간
    private boolean result; //true, false
    private Timestamp timestamp;
}