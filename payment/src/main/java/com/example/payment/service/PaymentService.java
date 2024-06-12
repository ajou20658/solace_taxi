package com.example.payment.service;

import com.example.payment.dto.PaymentRequest2User;
import com.example.payment.vo.PaymentRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solacesystems.jcsmp.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final XMLMessageProducer producer;
    private final ObjectMapper objectMapper;
    private final String payTopic = "gwangbu/PaymentQueue";
//    private final Queue queue = JCSMPFactory.onlyInstance().createQueue(payTopic);
    private final Topic topic = JCSMPFactory.onlyInstance().createTopic(payTopic);
    public void sendCustomerPayment(PaymentRequest paymentRequest){
        TextMessage textMessage = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        textMessage.setDeliveryMode(DeliveryMode.DIRECT);
        try{
            SDTMap map = producer.createMap();
            map.putString("user",paymentRequest.getUserId());
            textMessage.setProperties(map);
            PaymentRequest2User paymentRequest2User = new PaymentRequest2User();
            paymentRequest2User.setCost(paymentRequest.getCost());
            paymentRequest2User.setServiceType(paymentRequest.getServiceType());
            paymentRequest2User.setTimestamp(paymentRequest.getTimestamp());
            sendTopic(textMessage,paymentRequest2User,topic);
            log.info("[결제 요청 전달] UserId:{}, Cost:{}, ServiceType : {}"
                    ,paymentRequest.getUserId(),paymentRequest.getCost(),paymentRequest.getServiceType());
        }catch (JCSMPException ex){
            log.error(ex.getMessage());
        }catch (JsonProcessingException ex){
            log.error(ex.getMessage());
        }
    }
//    private void sendQueue(TextMessage textMessage, Object object, Queue queue) throws JCSMPException, JsonProcessingException {
//        String text = objectMapper.writeValueAsString(object);
//        textMessage.setText(text);
//        producer.send(textMessage,queue);
//    }
    private void sendTopic(TextMessage textMessage, Object object, Topic topic) throws JCSMPException, JsonProcessingException {
        String text = objectMapper.writeValueAsString(object);
        textMessage.setText(text);
        producer.send(textMessage,topic);
    }
}
