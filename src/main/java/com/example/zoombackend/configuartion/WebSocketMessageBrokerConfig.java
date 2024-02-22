package com.example.zoombackend.configuartion;

import jakarta.annotation.Nonnull;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Log4j2
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketMessageBrokerConfig implements WebSocketMessageBrokerConfigurer {
    private final Integer MESSAGE_LENGTH = 1024 * 1024;

    @Override
    public void configureMessageBroker(@Nonnull MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic/", "/queue/");
        config.setApplicationDestinationPrefixes("/app");
        config.setPreservePublishOrder(true);
    }

    @Override
    public void registerStompEndpoints(@Nonnull StompEndpointRegistry registry) {
        registry.addEndpoint("/")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(@Nonnull ChannelRegistration registration) {
        registration.taskExecutor().corePoolSize(1);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(MESSAGE_LENGTH);
        registration.setSendBufferSizeLimit(MESSAGE_LENGTH);
    }

    @Bean
    public ServletServerContainerFactoryBean createservletservercontainerfactorybean() {
        ServletServerContainerFactoryBean factoryBean = new ServletServerContainerFactoryBean();
        factoryBean.setMaxTextMessageBufferSize(MESSAGE_LENGTH);
        factoryBean.setMaxBinaryMessageBufferSize(MESSAGE_LENGTH);
        return factoryBean;
    }
}
