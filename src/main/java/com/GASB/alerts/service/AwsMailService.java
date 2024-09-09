package com.GASB.alerts.service;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsMailService{

    private final AmazonSimpleEmailService amazonSimpleEmailService;

    @RabbitListener(queues = "#{@rabbitMQProperties.suspiciousQueue}")
    public void send() {
        final String ATTACHMENT = null;
        try {
            SendRawEmailRequest sendRawEmailRequest = MailUtil.getSendRawEmailRequest("제목입니당", "메세지입니당", "lee39095296@gmail.com", ATTACHMENT);
            amazonSimpleEmailService.sendRawEmail(sendRawEmailRequest);
        }catch (Exception e){
            log.info("Email Failed");
            log.info("Error message: " + e.getMessage());
        }
    }
}
