package com.example.driver.dto;

import com.example.driver.vo.Location;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class PickupRequest {
    private String rideId;
    private Location currentLocation;
    private Location destination;
    private Timestamp timestamp;
}