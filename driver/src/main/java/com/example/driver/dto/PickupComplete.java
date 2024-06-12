package com.example.driver.dto;

import com.example.driver.vo.Location;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class PickupComplete {
    private String driverId;
    private Location location;
    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());
}
