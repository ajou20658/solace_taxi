package com.example.platform.config;

import com.solacesystems.jcsmp.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class SolaceConfig {
    private final Environment env;
    private final PublishHandler publishHandler;
    @Bean
    public JCSMPSession session() throws JCSMPException {
        JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.USERNAME,env.getProperty("solace.username"));
        properties.setProperty(JCSMPProperties.PASSWORD,env.getProperty("solace.password"));
        properties.setProperty(JCSMPProperties.VPN_NAME,env.getProperty("solace.vpn"));
        properties.setProperty(JCSMPProperties.HOST,env.getProperty("solace.host"));
        properties.setProperty(JCSMPProperties.CLIENT_NAME,"gwangbu_platform");
        JCSMPSession session = JCSMPFactory.onlyInstance().createSession(properties);
        session.connect();
        log.info("Session Connected : "+ session);
        return session;
    }
    @Bean
    public XMLMessageProducer publisher(JCSMPSession session) throws JCSMPException {
        return session.getMessageProducer(publishHandler);
    }
}