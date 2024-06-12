package com.example.platform.jpa;

import com.example.platform.vo.Location;
import com.example.platform.vo.RideRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class JpaService {
    private final LogRepository logRepository;
    private final String LOCATION_PREFIX = "AREA_";
    public String saveLog(RideRequest rideRequest){ // 첫 호출 로그
        String rideId = UUID.randomUUID().toString();
        ServiceLog serviceLog = new ServiceLog();
        serviceLog.setRideId(rideId);
        serviceLog.setUserId(rideRequest.getUserId());
        log.info(rideRequest.getUserId());
        serviceLog.setCurrent(rideRequest.getCurrentLocation());
        serviceLog.setDestination(rideRequest.getDestination());
        serviceLog.setEstimatedCost(estimateCost(rideRequest.getCurrentLocation(),rideRequest.getDestination()));
        logRepository.save(serviceLog);
        return rideId;
    }
    private Integer estimateCost(Location start, Location end){
        return Math.abs((int) (parser(end) - parser(start)));
    }
    private long parser(Location location){
        return Long.parseLong(location.toString().replace(LOCATION_PREFIX,""));
    }
    public void updateLogwithDriverId(ServiceLog serviceLog, String driverId){
        serviceLog.setDriverId(driverId);
        logRepository.save(serviceLog);
    }

    public ServiceLog findByRideId(String rideId){
        Optional<ServiceLog> serviceLog = logRepository.findById(rideId);
        if(serviceLog.isEmpty()){
            log.error("Not exists RideId : {}",rideId);
            return null;
        }else{
            return serviceLog.get();
        }
    }
    public ServiceLog findByDriverId(String driverId){
        Optional<ServiceLog> serviceLog = logRepository.findTopByDriverId(driverId);
        if(serviceLog.isEmpty()){
            log.error("Not exists");
            return null;
        }else{
            return serviceLog.get();
        }
    }
}
