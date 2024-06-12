package com.example.driver;

import ch.qos.logback.core.util.Loader;
import com.example.driver.config.Initiator;
import com.example.driver.service.DriverService;
import com.example.driver.vo.Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class DriverController {
    private final DriverService driverService;
    private final Initiator initiator;
    private final BufferedReader bufferedReader;
    @PostMapping("/dropOff")
    public ResponseEntity<Object> dropOffComplete(){
        Location now = initiator.getLocation();
        Location destination = initiator.getDestinationAddr();
        if(now.equals(destination) && initiator.isPassengerPickedUp()){
            driverService.dropoffComplete();
            return ResponseEntity.ok().body("하차 완료. 정산 요청을 보냅니다.");
        }else if(initiator.isPassengerPickedUp()) {
            try {
                System.out.println("목적지가 아닙니다. 승객이 원하는 정차지역이 맞습니까?");
                String textResponse = bufferedReader.readLine();
                if(textResponse.equals("YES")){
                    driverService.dropoffComplete();
                    return ResponseEntity.ok().body("하차 완료. 정산 요청을 보냅니다.");
                }

            }catch (IOException ex){
                log.error(ex.getMessage());
                return ResponseEntity.internalServerError().body("정산 요청 중 오류 발생. 다시시도 해주세요.");
            }
        }
        return ResponseEntity.badRequest().body("목적지에 도착하지 않았습니다.");
    }
    @PostMapping("/pickupComplete")
    public ResponseEntity<Object> pickupComplete(){
        Location now = initiator.getLocation();
        Location source = initiator.getSourceAddr();
        if(now.equals(source) && !initiator.isPassengerPickedUp()){
            driverService.pickupComplete();
            return ResponseEntity.ok().body("승차 완료.");
        }else if(!initiator.isPassengerPickedUp()){
            try{
                System.out.println("출발지가 아닙니다. 승객이 탑승한 것이 맞나요?");
                String textResponse = bufferedReader.readLine();
                if(textResponse.equals("YES")){
                    driverService.dropoffComplete();
                    return ResponseEntity.ok().body("승차 완료.");
                }{
                    return ResponseEntity.badRequest().body("출발지에 도착하지 않았습니다.");
                }
            }catch (IOException ex){
                log.error(ex.getMessage());
                return ResponseEntity.internalServerError().body("요청 중 오류 발생. 다시시도 해주세요.");
            }
        }
        return ResponseEntity.badRequest().body("다른 승객이 하차하지 않았습니다.");
    }
}
