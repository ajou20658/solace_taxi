package com.example.driver.dto;

import com.example.driver.vo.Location;
import com.example.driver.vo.Status;
import lombok.Data;

@Data
public class DriverLocation {
    private String driverId;
    private Location location;
    private Status status;
}
