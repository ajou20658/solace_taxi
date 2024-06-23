package com.example.platform.service;

import com.example.platform.dto.PaymentRequest;
import com.example.platform.dto.PickupRequest;
import com.example.platform.jpa.JpaService;
import com.example.platform.jpa.ServiceLog;
import com.example.platform.jpa.TaxiInfo;
import com.example.platform.vo.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solacesystems.jcsmp.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.lang.Math.abs;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlatformServiceImpl implements PlatformService{
//    private final JCSMPSession session;
    private final XMLMessageProducer producer;
    private final JpaService jpaService;
    private final TaxiService taxiService;
    private final ObjectMapper objectMapper;
    private final String PICKUP_PREFIX = "gwangbu/pickup/request/"; //토픽
//    private final Queue replyQueue = JCSMPFactory
    private final String PICKUP_RESPONSE_QUEUE = "gwangbu/PickupResponse";
    private final Queue pickupResponseQueue = JCSMPFactory.onlyInstance().createQueue(PICKUP_RESPONSE_QUEUE);
    private final String RIDERESPONSE_PREFIX = "gwangbu/ride/response/"; // 토픽
    private final String PAYMENT_PREFIX = "gwangbu/pay/request/server"; // 큐
    private final String LOCATION_PREFIX = "AREA_";
    private final Topic paymentTopic = JCSMPFactory.onlyInstance().createTopic(PAYMENT_PREFIX);
    @Override
    public void RideRequest2PickupRequest(RideRequest rideRequest) throws RuntimeException{
        // 여기서는 클라이언트의 RideRequest 객체를 받아와서 PickupRequest로 변환하여 전달
        String userId = rideRequest.getUserId();
        String rideId = jpaService.saveLog(rideRequest);
        System.out.printf("""
                [택시 호출 발생]
                * 탑승번호: %s
                * 발생한 호출 요청 userId: %s
                * 현재 위치: %s
                * 도착 위치: %s
                """
                ,rideId,userId,rideRequest.getCurrentLocation(),rideRequest.getDestination());
        PickupRequest pickupRequest = new PickupRequest().Ride2Pickup(rideRequest,rideId);
        sendPickupRequest(pickupRequest);
    }
    private void sendPickupRequest(PickupRequest pickupRequest) {
        TextMessage textMessage = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        textMessage.setDeliveryMode(DeliveryMode.DIRECT);
        List<TaxiInfo> taxiInfos = taxiService.getDriverIdList();
        if(!taxiInfos.isEmpty()){
            Location sourceAddr = pickupRequest.getCurrentLocation();
            // 등록된 운전자 정보를 받아온 후, 제일 가까운 운전자의 ID를 토픽을 만들어 전달 -> 큐로 만들면 오버헤드가 발생할거같아서 토픽으로 구현함.
            String driverId = getNearestDriverId(taxiInfos,sourceAddr);
            String driverTopic = PICKUP_PREFIX+driverId;
            // 동적인 토픽이라서 로컬에서 처리
            Topic pickupRequestTopic = JCSMPFactory.onlyInstance().createTopic(driverTopic);
            System.out.printf("""
            [택시 매칭 시도]
            * 매칭 번호: %s
            * 기사 번호: %s
            """,pickupRequest.getRideId(),driverId);
            try {
                sendTopic(textMessage, pickupRequest, pickupRequestTopic);//송신
                //수신
            }catch (JCSMPException ex){
                log.error(ex.getMessage());
            }catch (JsonProcessingException ex){
                log.error(ex.getMessage());
            }
        }else{
            System.out.printf("""
            [택시 매칭 시도]
            * 매칭 번호: %s
            * 대기 중인 택시 없음 (요청 실패)
            """,pickupRequest.getRideId());
            throw new RuntimeException();
        }
    }
    private String getNearestDriverId(List<TaxiInfo> taxiInfos, Location location){
        long targetLocation = parser(location);
        long distance = 6L;
        String driverId = "";
        for(TaxiInfo i : taxiInfos){
            int minus = (int) (targetLocation - parser(i.getAreaCode()));
            if(distance > abs(minus)){
                distance = abs(minus);
                driverId = i.getDriverId();
            }
        }
        return driverId;
    }
    private long parser(Location location){
        return Long.parseLong(location.toString().replace(LOCATION_PREFIX,""));
    }

    // 수락된 경우만 PickupResponse2RideResponse 호출됨 -> 로그 기록
    @Override
    public void sendRideResponse(PickupResponse pickupResponse) {
        TextMessage textMessage = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        textMessage.setDeliveryMode(DeliveryMode.DIRECT);

        ServiceLog serviceLog = jpaService.findByRideId(pickupResponse.getRideId());
        String rideTopic = RIDERESPONSE_PREFIX+serviceLog.getUserId();
        // 동적 토픽이라서 로컬에서 처리
        Topic rideResponseTopic = JCSMPFactory.onlyInstance().createTopic(rideTopic);
        try {
            sendTopic(textMessage, pickupResponse, rideResponseTopic);
        }catch (JCSMPException ex){
            log.error(ex.getMessage());
        }catch (JsonProcessingException ex){
            log.error(ex.getMessage());
        }
    }
    @Override
    public void sendPaymentRequest(DropoffComplete dropoffComplete) throws JCSMPException {
        TextMessage textMessage = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        textMessage.setDeliveryMode(DeliveryMode.PERSISTENT);
        PaymentRequest paymentRequest = new PaymentRequest();

        ServiceLog serviceLog = jpaService.findByDriverId(dropoffComplete.getDriverId());
        paymentRequest.setCost(serviceLog.getEstimatedCost());
        paymentRequest.setUserId(serviceLog.getUserId());
        paymentRequest.setServiceType(ServiceType.TAXI);
        try{
            sendTopic(textMessage,paymentRequest,paymentTopic);
            log.info(paymentRequest.toString());
        }catch (JsonProcessingException ex){
            log.error(ex.getMessage());
        }catch (JCSMPException ex){
            throw new JCSMPException(ex.getMessage(), ex);
        }
    }
    private void sendTopic(TextMessage textMessage,Object object, Topic topic) throws JsonProcessingException, JCSMPException {
        String text = objectMapper.writeValueAsString(object);
        textMessage.setText(text);
        producer.send(textMessage,topic);
    }
}
