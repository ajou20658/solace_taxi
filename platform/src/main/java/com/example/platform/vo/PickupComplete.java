package com.example.platform.vo;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class PickupComplete {
    private String driverId;
    private Location location;
    private Timestamp timestamp;
}
