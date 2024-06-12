package com.example.user.vo;

import lombok.Data;

@Data
public class UserRideRequest {
    private Location currentLocation;
    private Location destination;
}
