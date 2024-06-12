package com.example.platform.vo;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class DropoffComplete {
    private String driverId;
    private Timestamp timestamp;
}
