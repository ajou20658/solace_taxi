package com.example.platform.service;

import com.example.platform.jpa.JpaService;
import com.example.platform.jpa.ServiceLog;
import com.example.platform.vo.PickupResponse;
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
public class PickupResponseConsumer {
    private final ObjectMapper objectMapper;
    private final JCSMPSession session;
    private final PlatformService platformService;
    private final JpaService jpaService;
    private FlowReceiver cons;
    @PostConstruct
    void init(){
        try{
            Queue queue = JCSMPFactory.onlyInstance().createQueue("gwangbu/PickupResponse");

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
                        log.info(textMessage.getText());
                        try{
                            PickupResponse pickupResponse = objectMapper.readValue(textMessage.getText().getBytes(), PickupResponse.class);
                            resolveResponse(pickupResponse);
                            bytesXMLMessage.ackMessage();
                        }catch (IOException ex){
                            log.error("JSON 변환 오류: " + ex.getMessage());
                        }catch (JCSMPException ex){
                            log.error("RideResponse -> User 전송 오류");
                        }
                    }
                }
                @Override
                public void onException(JCSMPException e) {

                }
            },consumerFlowProperties,endpointProperties);
            cons.start();
        }catch (JCSMPException ex){
            log.error("JCSMP 에러 발생");
        }
    }
    private void resolveResponse(PickupResponse pickupResponse) throws JCSMPException{
        System.out.printf("""
                [택시 매칭 정보]
                * 매칭 번호: %s
                * 기사 번호: %s
                * 현재 위치: %s
                """
                ,pickupResponse.getRideId(),pickupResponse.getDriveId(),pickupResponse.isResult());
        ServiceLog serviceLog = jpaService.findByRideId(pickupResponse.getRideId());
        // ServiceLog에 저장 운전자 정보 반영 필요
        // DB에 존재하는 택시 호출 기록에 (rideId, userId) driverId를 추가시켜줌.(완료)
        // 이후 DropOffComplete 발견시, 결제 요청 처리하게끔
        jpaService.updateLogwithDriverId(serviceLog, pickupResponse.getDriveId());
        if(pickupResponse.isResult()){
            platformService.sendRideResponse(pickupResponse);
        }else{
            // 거절했다면 last-valued 큐로 전달. (구현 예정)
        }
    }
}
