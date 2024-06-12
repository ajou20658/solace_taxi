package com.example.platform.service;

import com.example.platform.vo.DriverInfo;
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
public class LocationUpdateConsumer {
    private final ObjectMapper objectMapper;
    private final JCSMPSession session;
    private final TaxiService taxiService;
    private FlowReceiver cons;
    @PostConstruct
    void init(){
        try{
            Queue queue = JCSMPFactory.onlyInstance().createQueue("gwangbu/LocationUpdate");
            EndpointProperties endpointProperties = new EndpointProperties();
            endpointProperties.setPermission(EndpointProperties.PERMISSION_CONSUME);
            endpointProperties.setAccessType(EndpointProperties.ACCESSTYPE_NONEXCLUSIVE);
            endpointProperties.setRespectsMsgTTL(true);

            ConsumerFlowProperties consumerFlowProperties = new ConsumerFlowProperties();
            consumerFlowProperties.setEndpoint(queue);
            consumerFlowProperties.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);

            session.provision(queue,endpointProperties,JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);

            cons = session.createFlow(new XMLMessageListener() {
                @Override
                public void onReceive(BytesXMLMessage bytesXMLMessage) {
                    if (bytesXMLMessage instanceof TextMessage textMessage){
//                        log.info(textMessage.getText());
                        try{
                            DriverInfo request = objectMapper.readValue(textMessage.getText().getBytes(), DriverInfo.class);
                            log.info("[DriverId: {}] 현재 위치 : {}, 현재 상태 : {}"
                                    ,request.getDriverId(),request.getLocation(),request.getStatus());
                            taxiService.addService(request);
                            bytesXMLMessage.ackMessage();
                        } catch (IOException ex){
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
