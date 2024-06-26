package com.example.user.service;

import com.example.user.config.Initiator;
import com.example.user.vo.Location;
import com.example.user.vo.PaymentRequest;
import com.example.user.vo.RideRequest;
import com.example.user.vo.RideResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solacesystems.jcsmp.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService{
    private final XMLMessageProducer producer;
    private final String rideRequest = "gwangbu/RideRequest"; //송신
//    private final Queue rideRequestQueue = JCSMPFactory.onlyInstance().createQueue(rideRequest);
    private final Topic rideRequestTopic = JCSMPFactory.onlyInstance().createTopic(rideRequest);
    private final ObjectMapper objectMapper;
    private final BufferedReader bufferedReader;
    private final Initiator initiator;

    @Override
    public void rideRequest() throws JCSMPException {
        Location current = Location.getRandomLocation();
        Location destination = Location.getRandomLocation();
        RideRequest request = new RideRequest();
        request.setUserId(initiator.getUserId());
        request.setCurrentLocation(current);
        while (destination == current) {
            destination = Location.getRandomLocation();
        }
        request.setDestination(destination);
        TextMessage textMessage = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        textMessage.setDeliveryMode(DeliveryMode.PERSISTENT);
        textMessage.setTimeToLive(10000L);
        try {
            sendTopic(textMessage,request,rideRequestTopic);
            System.out.printf("""
                    [택시호출]
                    * userId : %s
                    * 출발지 : %s
                    * 목적지 : %s
                    """
                    ,request.getUserId(),request.getCurrentLocation(),request.getDestination());
        }catch (IOException ex){
            log.error(ex.getMessage());
        }
    }

    @Override
    public void handleRideResponse(RideResponse rideResponse) {
        System.out.println(rideResponse.getDriveId()+" 번호 택시 매칭됨.");
    }

    @Override
    public void handlePaymentRequest(PaymentRequest paymentRequest){
        try{
            System.out.printf("""
                    [정산 요청 도착]
                    * 서비스 유형 : %s
                    * 결제 금액 : %d원
                    """,
                    paymentRequest.getServiceType(),
                    paymentRequest.getCost());
            // Yes 하면 큐에서 빼고, NO하면 큐에 남아있도록
            System.out.print("""
                    * 결제 승인
                    * YES - 카드 결제
                    * NO - 결제 재시도
                    * CASH - 현금 결제
                    > """);
            String textInput = bufferedReader.readLine();
            if(textInput.equals("NO")){
                throw new RuntimeException();
            } else if (textInput.equals("YES")) {
                System.out.printf("%d원이 결제되었습니다.\n",paymentRequest.getCost());
            }
        }catch (IOException ex){
            log.error(ex.getMessage());
        }
    }
    private void sendTopic(TextMessage textMessage, Object object, Topic topic) throws JCSMPException, JsonProcessingException {
        String text = objectMapper.writeValueAsString(object);
        textMessage.setText(text);
        producer.send(textMessage,topic);
    }
}
