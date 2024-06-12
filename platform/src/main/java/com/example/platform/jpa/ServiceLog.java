package com.example.platform.jpa;

import com.example.platform.vo.Location;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Data
public class ServiceLog {
    @Id
    private String rideId;
    private String userId;
    @Enumerated(value = EnumType.STRING)
    private Location current;
    @Enumerated(value = EnumType.STRING)
    private Location destination;
    private Integer estimatedCost;
    private String driverId;
    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());
}
