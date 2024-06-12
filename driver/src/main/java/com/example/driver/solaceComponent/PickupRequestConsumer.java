package com.example.driver.solaceComponent;

import com.example.driver.config.Initiator;
import com.example.driver.service.DriverService;
import com.example.driver.dto.PickupRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solacesystems.jcsmp.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class PickupRequestConsumer {
    private final Initiator initiator;
    private final String PICKUP_PREFIX = "gwangbu/pickup/request/";
    private final JCSMPSession session;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final DriverService driverService;
    private XMLMessageConsumer consumer;
    @PostConstruct
    void init(){
        try{
            consumer = session.getMessageConsumer(new XMLMessageListener(){
                @Override
                public void onReceive(BytesXMLMessage bytesXMLMessage) {
                    if(bytesXMLMessage instanceof TextMessage textMessage){
                        log.info(textMessage.getText());
                        try{
                            PickupRequest pickupRequest = objectMapper.readValue(textMessage.getText().getBytes(), PickupRequest.class);
                            log.info("PickupRequest : {}",pickupRequest);
                            driverService.pickupRequest2Response(pickupRequest);
                            bytesXMLMessage.ackMessage();
                        }catch (IOException ex){
                            log.error(ex.getMessage());
                        }
                    }
                }

                @Override
                public void onException(JCSMPException e) {

                }
            }

            );
            String driverTopic = PICKUP_PREFIX+initiator.getDriverId();
            Topic topic = JCSMPFactory.onlyInstance().createTopic(driverTopic);
            log.info("subTopic from platform : {}",driverTopic);
            session.addSubscription(topic);
            consumer.start();
        }catch (JCSMPException ex){
            log.error("구독 중 오류 발생");
            ex.printStackTrace();
        }
    }

}
