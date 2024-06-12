package com.example.user.vo;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class RideRequest {
    private String userId;
    private Location currentLocation;
    private Location destination;
    private Timestamp timestamp;
    {
        timestamp = new Timestamp(System.currentTimeMillis());
    }
}
