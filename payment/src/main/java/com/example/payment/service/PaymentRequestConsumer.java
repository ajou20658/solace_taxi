package com.example.payment.service;

import com.example.payment.vo.PaymentRequest;
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
public class PaymentRequestConsumer {
    private final JCSMPSession session;
    private final PaymentService paymentService;
    private final String PAYMENT = "gwangbu/PaymentRequest";
    private FlowReceiver flowReceiver;
    private final ObjectMapper objectMapper;
    @PostConstruct
    void init(){
        try{
            Queue queue = JCSMPFactory.onlyInstance().createQueue(PAYMENT);
            EndpointProperties endpointProperties = new EndpointProperties();
            endpointProperties.setPermission(EndpointProperties.PERMISSION_CONSUME);
            endpointProperties.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);

            ConsumerFlowProperties consumerFlowProperties = new ConsumerFlowProperties();
            consumerFlowProperties.setEndpoint(queue);
            consumerFlowProperties.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);
            session.provision(queue,endpointProperties,JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
            flowReceiver = session.createFlow(new XMLMessageListener() {
                @Override
                public void onReceive(BytesXMLMessage bytesXMLMessage) {
                    if(bytesXMLMessage instanceof TextMessage textMessage){
                        try{
                            PaymentRequest paymentRequest = objectMapper.readValue(textMessage.getText().getBytes(), PaymentRequest.class);
//                            log.info(paymentRequest.toString());
                            paymentService.sendCustomerPayment(paymentRequest);
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
            flowReceiver.start();
        }catch (JCSMPException ex){
            log.error(ex.getMessage());
        }
    }
}
