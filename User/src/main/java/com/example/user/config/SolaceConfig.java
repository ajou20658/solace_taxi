package com.example.user.config;

import com.example.user.eventHandler.PublisherHandler;
import com.solacesystems.jcsmp.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@RequiredArgsConstructor
public class SolaceConfig {
    private final Environment env;
    private final PublisherHandler publisherHandler;
    @Bean
    public JCSMPSession session() throws JCSMPException{
        JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.USERNAME,env.getProperty("solace.username"));
        properties.setProperty(JCSMPProperties.PASSWORD,env.getProperty("solace.password"));
        properties.setProperty(JCSMPProperties.VPN_NAME,env.getProperty("solace.vpn"));
        properties.setProperty(JCSMPProperties.HOST,env.getProperty("solace.host"));
        JCSMPSession session = JCSMPFactory.onlyInstance().createSession(properties);
        session.connect();
        System.out.println("Session Connected : "+ session);
        return session;
    }
    @Bean
    public XMLMessageProducer publisher(JCSMPSession session) throws JCSMPException {
        return session.getMessageProducer(publisherHandler);
    }
//    @Bean
//    public TextMessage msg(){
//        return JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
//    }
}
