package com.GASB.alerts.service;

import com.GASB.alerts.model.entity.AlertSettings;
import com.GASB.alerts.repository.*;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class AwsMailService{

    private final AmazonSimpleEmailService amazonSimpleEmailService;
    private final AlertEmailsRepo alertEmailsRepo;

    public void sendMail(List<AlertSettings> alertSettings){
        try {
            for(AlertSettings a : alertSettings) {
                List<String> receivers = alertEmailsRepo.findEmailByAlertId(a.getId());
                SendRawEmailRequest sendRawEmailRequest = MailUtil.getSendRawEmailRequest(a.getTitle(), a.getContent(), receivers);
                amazonSimpleEmailService.sendRawEmail(sendRawEmailRequest);
            }
        }catch (Exception e){
            log.info("Email Failed message: " + e.getMessage());
        }
    }
}
