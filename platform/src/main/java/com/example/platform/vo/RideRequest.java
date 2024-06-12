package com.example.platform.vo;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class RideRequest {
    private String userId;
    private Location currentLocation;
    private Location destination;
    private Timestamp timestamp;
}
