package com.example.platform.service;

import com.example.platform.jpa.JpaService;
import com.example.platform.vo.DropoffComplete;
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
public class DropOffCompleteConsumer {
    private final ObjectMapper objectMapper;
    private final JCSMPSession session;
    private final PlatformService platformService;
    private final String DROPOFFCOMPLETE = "gwangbu/DropoffComplete";
    private FlowReceiver flowReceiver;
    @PostConstruct
    void init(){
        try{
            Queue queue = JCSMPFactory.onlyInstance().createQueue(DROPOFFCOMPLETE);
            EndpointProperties endpointProperties = new EndpointProperties();
            endpointProperties.setPermission(EndpointProperties.PERMISSION_CONSUME);
            endpointProperties.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);

            ConsumerFlowProperties consumerFlowProperties = new ConsumerFlowProperties();
            consumerFlowProperties.addRequiredSettlementOutcomes(XMLMessage.Outcome.FAILED);
            consumerFlowProperties.setEndpoint(queue);
            consumerFlowProperties.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);
            session.provision(queue,endpointProperties,JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
            flowReceiver = session.createFlow(new XMLMessageListener() {
                @Override
                public void onReceive(BytesXMLMessage bytesXMLMessage) {
                    if(bytesXMLMessage instanceof TextMessage textMessage){
                        try{
                            DropoffComplete dropoffComplete = objectMapper.readValue(textMessage.getText().getBytes(), DropoffComplete.class);
                            platformService.sendPaymentRequest(dropoffComplete);
                            bytesXMLMessage.ackMessage();
                        }catch (IOException ex){
                            log.error("JSON 변환 오류: "+ ex.getMessage());
                        }catch (JCSMPException ex){
                            try {
                                bytesXMLMessage.settle(XMLMessage.Outcome.FAILED);
                            } catch (JCSMPException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }

                @Override
                public void onException(JCSMPException e) {

                }
            },consumerFlowProperties,endpointProperties);
            flowReceiver.start();
        }catch (JCSMPException ex){
            log.error(ex.getMessage());
        }
    }
}
