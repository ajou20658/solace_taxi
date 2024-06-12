package com.example.user.config;

import com.example.user.vo.Location;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Getter
@Setter
public class Initiator {
    private final Location location;
    private final String userId;
    public Initiator(){
        this.location = Location.getRandomLocation();
        this.userId = UUID.randomUUID().toString();
    }
}
