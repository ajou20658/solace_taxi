package com.example.payment.config;

import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PublishHandler implements JCSMPStreamingPublishCorrelatingEventHandler {
    @Override
    public void responseReceivedEx(Object o) {
        if ( o != null) {
            log.debug("Ex> Producer received response for msg: " + o.toString());
        } else {
            log.debug("Ex> Producer received response, but o is null");
        }
    }

    @Override
    public void handleErrorEx(Object o, JCSMPException e, long l) {
        log.error("연결 오류 발생");
    }
}
