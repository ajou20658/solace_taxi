package com.example.platform.service;

import com.example.platform.vo.RideRequest;
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
public class RideRequestConsumer {
    // RideRequest(gwangbu/RideRequest), PickupRequestResponse,PickupComplete 구독
    // 큐는 flow 만들기, 토픽은 addsubscription
//    private final ModelMapper modelMapper = new ModelMapper();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JCSMPSession session;
    private final PlatformService platformService;
    private FlowReceiver cons;
    @PostConstruct
    public void init(){
        try {
            Queue queue = JCSMPFactory.onlyInstance().createQueue("gwangbu/RideRequest");

            EndpointProperties endpointProperties = new EndpointProperties();
            endpointProperties.setPermission(EndpointProperties.PERMISSION_CONSUME);
            endpointProperties.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);
            endpointProperties.setRespectsMsgTTL(true);

            ConsumerFlowProperties flowProperties = new ConsumerFlowProperties();
            flowProperties.addRequiredSettlementOutcomes(XMLMessage.Outcome.FAILED);
            flowProperties.setEndpoint(queue);
            flowProperties.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);
            session.provision(queue,endpointProperties,JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
            cons = session.createFlow(new XMLMessageListener() {
                @Override
                public void onReceive(BytesXMLMessage bytesXMLMessage) {
                    if (bytesXMLMessage instanceof TextMessage message) {
                        log.info(message.getText());
                        try {
                            RideRequest request = objectMapper.readValue(message.getText().getBytes(), RideRequest.class);
                            platformService.RideRequest2PickupRequest(request);
                            bytesXMLMessage.ackMessage();
                        } catch (JCSMPException ex) {
                            log.error(ex.getMessage());
                        } catch (IOException e) { // JSON 처리 중 발생할 수 있는 예외 처리
                            log.error("JSON 변환 오류: " + e.getMessage());
                        } catch (RuntimeException e){
                            try {
                                bytesXMLMessage.settle(XMLMessage.Outcome.FAILED);
                            }catch (JCSMPException ex){
                                log.error(ex.getMessage());
                            }
                        }
                    }
                }

                @Override
                public void onException(JCSMPException e) {
                    log.error("error is "+e.getMessage());
                }
            },flowProperties,endpointProperties);
            cons.start();
        }catch (Exception exception){
            log.error(exception.toString());
        }
    }
}
