package com.example.user.eventHandler;

import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PublisherHandler implements JCSMPStreamingPublishCorrelatingEventHandler {
    @Override
    public void responseReceivedEx(Object o) {
        if ( o != null) {
            log.debug("Ex> Producer received response for msg: " + o.toString());
        } else {
            log.debug("Ex> Producer received response");
        }
    }

    @Override
    public void handleErrorEx(Object o, JCSMPException e, long l) {
        log.error("연결 오류 발생");
    }
    @Override
    public void handleError(String messageID, JCSMPException cause, long timestamp) {
        JCSMPStreamingPublishCorrelatingEventHandler.super.handleError(messageID, cause, timestamp);
    }

    @Override
    public void responseReceived(String messageID) {
        JCSMPStreamingPublishCorrelatingEventHandler.super.responseReceived(messageID);
        log.debug("Ex> Producer received response for msg: "+ messageID);
    }
}
