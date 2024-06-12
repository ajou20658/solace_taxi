package com.example.platform.config;

import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;
import org.springframework.stereotype.Component;

@Component
public class PublishHandler implements JCSMPStreamingPublishCorrelatingEventHandler {
    @Override
    public void responseReceivedEx(Object o) {
        if ( o != null) {
            System.out.println("Ex> Producer received response for msg: " + o.toString());
        } else {
            System.out.println("Ex> Producer received response, but o is null");
        }
    }

    @Override
    public void handleErrorEx(Object o, JCSMPException e, long l) {
        System.out.println("연결 오류 발생");
    }
}
