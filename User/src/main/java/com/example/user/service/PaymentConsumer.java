package com.example.user.service;

import com.example.user.config.Initiator;
import com.example.user.vo.PaymentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solacesystems.jcsmp.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentConsumer {
    private final JCSMPSession session;
    private final Initiator initiator;
    private final UserService userService;
    private final String PAYMENT = "gwangbu/PaymentQueue";
    private FlowReceiver flowReceiver;
    private final ObjectMapper objectMapper;
//    private final int MAX_RETRIES = 0;
//    private final ConcurrentLinkedQueue<RetryMessage> retryQueue = new ConcurrentLinkedQueue<>();
    @PostConstruct
    void init(){
        try{
            Queue queue = JCSMPFactory.onlyInstance().createQueue(PAYMENT);
            EndpointProperties endpointProperties = new EndpointProperties();
            endpointProperties.setPermission(EndpointProperties.PERMISSION_CONSUME);
            endpointProperties.setAccessType(EndpointProperties.ACCESSTYPE_NONEXCLUSIVE);
            endpointProperties.setMaxMsgRedelivery(10);
//            endpointProperties.
            String userId = initiator.getUserId();

            ConsumerFlowProperties consumerFlowProperties = new ConsumerFlowProperties();
            consumerFlowProperties.setEndpoint(queue);
            consumerFlowProperties.addRequiredSettlementOutcomes(XMLMessage.Outcome.FAILED, XMLMessage.Outcome.REJECTED);
            consumerFlowProperties.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);
            consumerFlowProperties.setSelector("user = '" + userId + "'");
            session.provision(queue,endpointProperties,JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
            flowReceiver = session.createFlow(new XMLMessageListener() {
                @Override
                public void onReceive(BytesXMLMessage bytesXMLMessage) {
                    if(bytesXMLMessage instanceof TextMessage textMessage){
                        int result = -1;
                        try {
                            PaymentRequest paymentRequest = objectMapper.readValue(textMessage.getText().getBytes(), PaymentRequest.class);
                            userService.handlePaymentRequest(paymentRequest);
                            result = 1;
                        }catch (IOException ex){
                            log.error("JSON 변환 오류: "+ ex.getMessage());
                            result = 2;
                        }catch (RuntimeException ex){
                            result = 3;
                        }
                        try{
                            if (result == 1){
                                bytesXMLMessage.ackMessage();
                            } else if (result == 2) {
                                bytesXMLMessage.settle(XMLMessage.Outcome.REJECTED);
                            } else {
                                bytesXMLMessage.settle(XMLMessage.Outcome.FAILED);
                            }
                        }catch (JCSMPException ex){
                            log.error("JCSMP 예외 발생");
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

///
//    @Scheduled(fixedDelay = 10000) // 10초마다 재시도
//    private void processRetryQueue() {
//        while (!retryQueue.isEmpty()) {
//            RetryMessage retryMessage = retryQueue.poll();
//            if (retryMessage != null) {
//                try {
//                    PaymentRequest paymentRequest = objectMapper.readValue(((TextMessage) retryMessage.getMessage()).getText().getBytes(), PaymentRequest.class);
//                    userService.handlePaymentRequest(paymentRequest);
//                    retryMessage.getMessage().ackMessage();
//                } catch (IOException | RuntimeException ex) {
//                    log.error("재시도 중 오류 발생: " + ex.getMessage());
//                    retryMessage.incrementRetryCount();
//                    if (retryMessage.getRetryCount().get() < MAX_RETRIES) {
//                        retryQueue.add(retryMessage); // 재시도 큐에 다시 추가
//                    } else {
//                        log.error("재시도 횟수 초과. 메시지 처리 실패: " + ex.getMessage());
//                        // 재시도 횟수 초과한 메시지 처리 (예: 별도의 큐에 저장하거나 로그로 남기기)
//                    }
//                }
//            }
//        }
//    }
//    private final Persistent
}