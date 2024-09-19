package com.GASB.alerts.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    private final RabbitMQProperties properties;

    public RabbitMQConfig(RabbitMQProperties properties) {
        this.properties = properties;
    }
    // exchange settings
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(properties.getExchange());
    }

    // queue settings
    @Bean
    public Queue uploadQueue(){
        return new Queue(properties.getUploadQueue(), true, false, false);
    }

    @Bean
    public Queue eventSuspiciousQueue(){
        return new Queue(properties.getSuspiciousQueue(), true, false, false);
    }

    @Bean
    public Queue eventDlpQueue(){
        return new Queue(properties.getDlpQueue(), true, false, false);
    }

    @Bean
    public Queue eventVtQueue(){
        return new Queue(properties.getVtQueue(), true, false, false);
    }

    // binding settings
    @Bean
    public Binding uploadBinding(Queue uploadQueue, DirectExchange exchange){
        return BindingBuilder.bind(uploadQueue).to(exchange).with(properties.getUploadRoutingKey());
    }

    @Bean
    public Binding eventSuspiciousBinding(Queue eventSuspiciousQueue, DirectExchange exchange){
        return BindingBuilder.bind(eventSuspiciousQueue).to(exchange).with(properties.getSuspiciousRoutingKey());
    }

    @Bean
    public Binding eventDlpBinding(Queue eventDlpQueue, DirectExchange exchange){
        return BindingBuilder.bind(eventDlpQueue).to(exchange).with(properties.getDlpRoutingKey());
    }

    @Bean
    public Binding eventVtBinding(Queue eventVtQueue, DirectExchange exchange){
        return BindingBuilder.bind(eventVtQueue).to(exchange).with(properties.getVtRoutingKey());
    }

    // RabbitTemplate 설정
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setExchange(properties.getExchange());
        return rabbitTemplate;
    }
}
