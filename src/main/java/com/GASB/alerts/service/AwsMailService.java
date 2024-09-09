package com.GASB.alerts.service;

import com.GASB.alerts.model.entity.AlertSettings;
import com.GASB.alerts.repository.AdminUsersRepo;
import com.GASB.alerts.repository.AlertEmailsRepo;
import com.GASB.alerts.repository.AlertSettingsRepo;
import com.GASB.alerts.repository.FileUploadRepo;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class AwsMailService{

    private final AmazonSimpleEmailService amazonSimpleEmailService;
    private final AlertSettingsRepo alertSettingsRepo;
    private final FileUploadRepo fileUploadRepo;
    private final AlertEmailsRepo alertEmailsRepo;

    @RabbitListener(queues = "#{@rabbitMQProperties.vtQueue}")
    public void send(long uploadId) {
        Long orgId = fileUploadRepo.findOrgIdByUploadId(uploadId);
        List<AlertSettings> alertSettings = alertSettingsRepo.findAllByOrgIdAndVtTrue(orgId);
        try {
            for(AlertSettings a : alertSettings) {
                List<String> receivers = alertEmailsRepo.findEmailByAlertId(a.getId());
                SendRawEmailRequest sendRawEmailRequest = MailUtil.getSendRawEmailRequest(a.getTitle(), a.getContent(), receivers);
                amazonSimpleEmailService.sendRawEmail(sendRawEmailRequest);
            }
        }catch (Exception e){
            log.info("Email Failed");
            log.info("Error message: " + e.getMessage());
        }
    }
}
