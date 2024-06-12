package com.example.platform.vo;

import lombok.Data;

@Data
public class DriverInfo {
    private String driverId;
    private Location location;
    private Status status;
}
