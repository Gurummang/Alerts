package com.GASB.alerts.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitMQProperties {

    private String exchange;
    private String uploadQueue;
    private String uploadRoutingKey;
    private String suspiciousQueue;
    private String suspiciousRoutingKey;
    private String dlpQueue;
    private String dlpRoutingKey;
    private String vtQueue;
    private String vtRoutingKey;
}
