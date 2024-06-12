package com.example.platform.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Random;

public enum Location {
    AREA_1,AREA_2,AREA_3,AREA_4,AREA_5;
    public static Location getRandomLocation(){
        Location[] locations = Location.values();
        Random random = new Random();
        int index = random.nextInt(locations.length);
        return locations[index];
    }
}
