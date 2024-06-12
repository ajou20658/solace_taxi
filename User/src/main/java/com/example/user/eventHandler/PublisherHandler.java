package com.example.user.eventHandler;

import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;
import org.springframework.stereotype.Component;

@Component
public class PublisherHandler implements JCSMPStreamingPublishCorrelatingEventHandler {
    @Override
    public void responseReceivedEx(Object o) {
        if ( o != null) {
            System.out.println("Ex> Producer received response for msg: " + o.toString());
        } else {
            System.out.println("Ex> Producer received response");
        }
    }

    @Override
    public void handleErrorEx(Object o, JCSMPException e, long l) {
        System.out.println("연결 오류 발생");
    }
    @Override
    public void handleError(String messageID, JCSMPException cause, long timestamp) {
        JCSMPStreamingPublishCorrelatingEventHandler.super.handleError(messageID, cause, timestamp);
    }

    @Override
    public void responseReceived(String messageID) {
        JCSMPStreamingPublishCorrelatingEventHandler.super.responseReceived(messageID);
        System.out.println("Ex> Producer received response for msg: "+ messageID);
    }
}
