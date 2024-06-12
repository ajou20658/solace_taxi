package com.example.driver.service;

import com.example.driver.config.Initiator;
import com.example.driver.vo.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduledTask {
    private final DriverService driverService;
    private final Initiator initiator;
    private final String LOCATION_PREFIX="AREA_";
    @Scheduled(fixedRate = 5000)
    public void sendLocation(){
        driverService.sendLocationUpdateProducer();
    }
    @Scheduled(fixedRate = 5000)
    public void goDestination(){
        // 승객이 탑승했고, 목적지가 있는 경우 -> 목적지로 가는 스케줄러 작동
        if(initiator.isPassengerPickedUp()&&initiator.getDestinationAddr()!=null) {
            Location start = initiator.getLocation();
            Location end = initiator.getDestinationAddr();
            long start_v = parser(start);
            long end_v = parser(end);
            if (start_v != end_v) {
                if (start_v < end_v) {
                    start_v += 1;
                } else {
                    start_v -= 1;
                }
                Location location = Location.valueOf(LOCATION_PREFIX + start_v);
                if(start_v == end_v){
                    System.out.printf("[도착지]내 위치 : %s, 도착지 : %s\n",location,end);
                }
                initiator.setLocation(location);
            }


        }

    }
    @Scheduled(fixedRate = 5000)
    public void goSource(){
        // 승객이 아직 탑승하지 않았고, 출발지가 있는 경우 -> 출발지로 가는 스케줄러 작동
        if(!initiator.isPassengerPickedUp()&&initiator.getSourceAddr()!=null){
            Location start = initiator.getLocation();
            Location end = initiator.getSourceAddr();
            long start_v = parser(start);
            long end_v = parser(end);
            if (start_v != end_v) {
                if (start_v < end_v) {
                    start_v += 1;
                } else {
                    start_v -= 1;
                }
                Location location = Location.valueOf(LOCATION_PREFIX+start_v);
                if(start_v == end_v){
                    System.out.printf("[출발지]내 위치 : %s, 출발지 : %s \n",location,end);
                }
                initiator.setLocation(location);
            }


        }

    }
    // PREFIX를 없애 위치를 값으로 변환
    private long parser(Location location){
        return Long.parseLong(location.toString().replace(LOCATION_PREFIX,""));
    }
}
