package com.example.user;

import com.example.user.service.UserService;
import com.example.user.vo.RideRequest;
import com.example.user.vo.UserRideRequest;
import com.solacesystems.jcsmp.JCSMPException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {
//    private final UserServiceImpl_ex userService;
    private final UserService userService;
    private final ModelMapper modelMapper = new ModelMapper();
    @PostMapping("/taxicall")
    public ResponseEntity<Object> taxiCall(){
//        userService.createRequest();
//        RideRequest request = modelMapper.map(userRideRequest, RideRequest.class);
        try{
            userService.rideRequest();
        }catch (JCSMPException ex){
            log.error(ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();
    }
}
