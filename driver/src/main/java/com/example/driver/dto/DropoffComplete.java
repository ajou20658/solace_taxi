package com.example.driver.dto;

import com.example.driver.vo.Location;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class DropoffComplete {
    private String driverId;
    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());
}
