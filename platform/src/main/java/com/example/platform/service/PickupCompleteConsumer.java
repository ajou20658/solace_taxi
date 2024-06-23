package com.example.platform.service;

import com.example.platform.jpa.JpaService;
import com.example.platform.vo.PickupComplete;
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
public class PickupCompleteConsumer {
    private final ObjectMapper objectMapper;
    private final JCSMPSession session;
    private final TaxiService taxiService;
    private final JpaService jpaService;
    private final String PICKUPCOMPLETE = "gwangbu/PickupComplete";
    private FlowReceiver cons;
    @PostConstruct
    void init(){
        try{
            Queue queue = JCSMPFactory.onlyInstance().createQueue(PICKUPCOMPLETE);
            EndpointProperties endpointProperties = new EndpointProperties();
            endpointProperties.setPermission(EndpointProperties.PERMISSION_CONSUME);
            endpointProperties.setAccessType(EndpointProperties.ACCESSTYPE_NONEXCLUSIVE);

            ConsumerFlowProperties consumerFlowProperties = new ConsumerFlowProperties();
            consumerFlowProperties.setEndpoint(queue);
            consumerFlowProperties.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);
            session.provision(queue,endpointProperties,JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
            cons = session.createFlow(new XMLMessageListener() {
                @Override
                public void onReceive(BytesXMLMessage bytesXMLMessage) {
                    if(bytesXMLMessage instanceof TextMessage textMessage){
                        try{
                            PickupComplete pickupComplete  = objectMapper.readValue(textMessage.getText().getBytes(), PickupComplete.class);
                            String rideId = jpaService.findByDriverId(pickupComplete.getDriverId()).getRideId();
                            System.out.printf("""
                            [택시 픽업 완료]
                            * 매칭 번호: %s
                            * 기사 번호: %s
                            * 탑승 위치: %s
                            """,rideId,pickupComplete.getDriverId(),pickupComplete.getLocation());
                            bytesXMLMessage.ackMessage();
                        }catch (IOException ex){
                            log.error("JSON 변환 오류: "+ ex.getMessage());
                        }
                    }
                }
                @Override
                public void onException(JCSMPException e) {

                }
            },consumerFlowProperties,endpointProperties);
            cons.start();
        }catch (JCSMPException ex){
            log.error(ex.getMessage());
        }
    }
}
