package com.example.driver.config;

import com.example.driver.vo.Location;
import com.example.driver.vo.Status;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Getter
@Setter
public class Initiator {
    private final String driverId;
    private Location location;
    private Location sourceAddr;
    private Location destinationAddr;
    private Status status;
    private boolean isPassengerPickedUp;
    public Initiator (){
        location = Location.getRandomLocation();
        status = Status.IDLE;
        driverId = UUID.randomUUID().toString();
    }
}
