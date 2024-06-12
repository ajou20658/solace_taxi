package com.example.driver.service;

import com.example.driver.config.Initiator;
import com.example.driver.dto.*;
import com.example.driver.vo.Status;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solacesystems.jcsmp.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service@Slf4j
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService{
    private final XMLMessageProducer producer;
    private final JCSMPSession session;
    private final Initiator initiator;
    private final String pickupResponse = "gwangbu/PickupResponse";
    // 태웠음을 알리는 이벤트
    // non-exclusive, 여러 노드가 알아서 처리가능함(로드 밸런싱), 중복처리는 되면 안됨 -> 큐를 사용
//    private final Queue pickupResponseQueue = JCSMPFactory.onlyInstance().createQueue(pickupResponse);
    private final Topic pickupResponseTopic = JCSMPFactory.onlyInstance().createTopic(pickupResponse);
    // non-exclusive, 여러 노드가 알아서 처리가능함(로드 밸런싱), 중복처리는 되면 안됨 -> 큐를 사용
//    private final Queue locationUpdateQueue = JCSMPFactory.onlyInstance().createQueue("gwangbu/LocationUpdate");
    private final Topic locationUpdateTopic = JCSMPFactory.onlyInstance().createTopic("gwangbu/LocationUpdate");
    // non-exclusive, 결제 요청은 순서 보장 없이 가능, 중복처리는 안됨 -> 큐를 사용
//    private final Queue dropoffCompleteQueue = JCSMPFactory.onlyInstance().createQueue("gwangbu/DropoffComplete");
    private final Topic dropoffCompleteTopic = JCSMPFactory.onlyInstance().createTopic("gwangbu/DropoffComplete");
//    private final Queue pickupCompleteQueue = JCSMPFactory.onlyInstance().createQueue("gwangbu/PickupComplete");
    private final Topic pickupCompleteTopic = JCSMPFactory.onlyInstance().createTopic("gwangbu/PickupComplete");
    private final BufferedReader reader;
    private FlowReceiver cons;
    private ObjectMapper objectMapper = new ObjectMapper();
    @PostConstruct
    void init(){
        //첫 초기화할떄 전송
        sendLocationUpdateProducer();
    }
    @Override
    public void pickupRequest2Response(PickupRequest pickupRequest) {
        TextMessage textMessage = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        textMessage.setDeliveryMode(DeliveryMode.PERSISTENT);

        // 픽업 응답을 보내기
        if (initiator.getStatus().equals(Status.IDLE)){
            PickupResponse response = new PickupResponse();

            try{
                System.out.println("택시 호출 발생");
                System.out.println("1. 승객 위치 :"+pickupRequest.getCurrentLocation());
                System.out.println("2. 목적지 :"+pickupRequest.getDestination());
                System.out.print("수락하시겠습니까 (YES or NO)?");
                String textResponse = reader.readLine();
                if(textResponse.equals("YES")){
                    response.setResult(true);
                    int minus = Integer.parseInt(initiator.getLocation().toString().replace("AREA_",""))
                            -Integer.parseInt(pickupRequest.getDestination().toString().replace("AREA_",""));
                    response.setETA(Math.abs(minus));
                    response.setDriveId(initiator.getDriverId());
                    response.setRideId(pickupRequest.getRideId());

                    setPickupBusy(pickupRequest);
                }else{
                    response.setResult(false);
                }
            }catch (IOException ex){
                log.error(ex.getMessage());
            }
            try {
                sendTopic(textMessage,response,pickupResponseTopic);
            }catch (JsonProcessingException ex){
                log.error("Json 변환 오류 발생");
            }catch (JCSMPException ex){
                log.error(ex.getMessage());
            }finally {
                if(response.isResult()){
                    log.info("태우러 갈게");
                }else {
                    log.info("안 태울래");
                }
                System.out.println("요청 응답 완료");
            }
        }
    }

    @Override
    public void sendLocationUpdateProducer() {
        TextMessage textMessage = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        textMessage.setDeliveryMode(DeliveryMode.DIRECT);
        textMessage.setTimeToLive(5000);
        // dto 생성
        DriverLocation driverLocation = new DriverLocation();
        driverLocation.setDriverId(initiator.getDriverId());
        driverLocation.setLocation(initiator.getLocation());
        driverLocation.setStatus(initiator.getStatus());
        // 메세지 전송
        try{
            SDTMap sdtMap = producer.createMap();
            sdtMap.putString("driver",initiator.getDriverId());
            textMessage.setProperties(sdtMap);
            sendTopic(textMessage,driverLocation,locationUpdateTopic);
        }catch (IOException | JCSMPException ex){
            log.error(ex.getMessage());
        }
    }

    @Override
    public void pickupComplete() {
        TextMessage textMessage = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        textMessage.setDeliveryMode(DeliveryMode.PERSISTENT);
// dto 생성
        PickupComplete pickupComplete = new PickupComplete();
        pickupComplete.setDriverId(initiator.getDriverId());
        pickupComplete.setLocation(initiator.getLocation());
        setPickupComplete();
// 메세지 전송
        try{
            sendTopic(textMessage,pickupComplete,pickupCompleteTopic);
        }catch (IOException | JCSMPException ex){
            log.error(ex.getMessage());
        }
    }

    @Override
    public void dropoffComplete() {
        TextMessage textMessage = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        textMessage.setDeliveryMode(DeliveryMode.PERSISTENT);
        // dto 생성
        DropoffComplete dropoffComplete = new DropoffComplete();
        dropoffComplete.setDriverId(initiator.getDriverId());
        // 메세지 전송
        try {
            sendTopic(textMessage, dropoffComplete, dropoffCompleteTopic);
        }catch (JsonProcessingException ex){
            log.error("json 변환 오류");
            log.error(ex.getMessage());
        }catch (JCSMPException ex){
            log.error("전송 오류 발생");
            log.error(ex.getMessage());
        }
        //손님 내렸으니 한가해
        setIdle();
        System.out.println("목적지 도착");
    }
//    private void sendQueue(TextMessage textMessage,Object object, Queue queue) throws JsonProcessingException, JCSMPException {
//        String text = objectMapper.writeValueAsString(object);
//        textMessage.setText(text);
//        producer.send(textMessage,queue);
//    }
    private void sendTopic(TextMessage textMessage,Object object, Topic topic) throws JsonProcessingException, JCSMPException {
        String text = objectMapper.writeValueAsString(object);
        textMessage.setText(text);
        producer.send(textMessage,topic);
    }
    private void setPickupBusy(PickupRequest pickupRequest){
        //이제 손님 태우러감
        initiator.setStatus(Status.BUSY);
        initiator.setSourceAddr(pickupRequest.getCurrentLocation());
        initiator.setDestinationAddr(pickupRequest.getDestination());
    }
    private void setPickupComplete(){
        initiator.setPassengerPickedUp(true);
    }
    private void setIdle(){
        // 손님 내림
        initiator.setStatus(Status.IDLE);
        initiator.setSourceAddr(null);
        initiator.setDestinationAddr(null);
        initiator.setPassengerPickedUp(false);
    }
}
