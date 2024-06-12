package com.example.user.service;

import com.example.user.config.Initiator;
import com.example.user.vo.RideResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solacesystems.jcsmp.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideResponseConsumer {
    private final JCSMPSession session;
    private final Initiator initiator;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final String RIDERESPONSE = "gwangbu/ride/response/";
    @PostConstruct
    void init(){
        try{
            Topic rideResponseTopic = JCSMPFactory.onlyInstance().createTopic(RIDERESPONSE+initiator.getUserId());
            session.addSubscription(rideResponseTopic);
            XMLMessageConsumer consumer = session.getMessageConsumer(new XMLMessageListener() {
                @Override
                public void onReceive(BytesXMLMessage bytesXMLMessage) {
                    if(bytesXMLMessage instanceof TextMessage textMessage){
                        try {
                            RideResponse rideResponse = objectMapper.readValue(textMessage.getText().getBytes(), RideResponse.class);
                            userService.handleRideResponse(rideResponse);
                        }catch (IOException ex){
                            log.error(ex.getMessage());
                        }
                    }
                }
                @Override
                public void onException(JCSMPException e) {

                }
            });
            consumer.start();
        }catch (JCSMPException ex){
            log.error(ex.getMessage());
        }
    }

}
