package com.example.platform.dto;

import com.example.platform.vo.Location;
import com.example.platform.vo.RideRequest;
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
    public PickupRequest Ride2Pickup(RideRequest rideRequest, String rideId){
        PickupRequest pickupRequest = new PickupRequest();
        pickupRequest.setRideId(rideId);
        pickupRequest.setDestination(rideRequest.getDestination());
        pickupRequest.setCurrentLocation(rideRequest.getCurrentLocation());
        pickupRequest.setTimestamp(rideRequest.getTimestamp());
        return pickupRequest;
    }
}