package com.GASB.alerts.service;

import com.GASB.alerts.exception.EmailSendingException;
import com.GASB.alerts.model.entity.AlertSettings;
import com.GASB.alerts.model.entity.FileUpload;
import com.GASB.alerts.repository.*;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class AwsMailService {

    private final AmazonSimpleEmailService amazonSimpleEmailService;
    private final AlertEmailsRepo alertEmailsRepo;
    private final FileUploadRepo fileUploadRepo;
    private final PolicyRepo policyRepo;
    private final MailUtil mailUtil;

    public void sendMail(List<AlertSettings> alertSettings, long uploadId) {
        if (alertSettings == null || alertSettings.isEmpty()) {
            log.info("Alert settings are empty. No email will be sent.");
            return;
        }
        Optional<FileUpload> fileUploadOptional = fileUploadRepo.findById(uploadId);
        fileUploadOptional.ifPresentOrElse(fileUpload -> {
            try {
                for (AlertSettings a : alertSettings) {
                    List<String> receivers = alertEmailsRepo.findEmailByAlertId(a.getId());
                    SendRawEmailRequest sendRawEmailRequest = mailUtil.getSendRawEmailRequest(a.getTitle(), a.getContent(), receivers, fileUpload);
                    amazonSimpleEmailService.sendRawEmail(sendRawEmailRequest);
                }
            } catch (MessagingException | IOException e) {
                log.error("Email sending failed: {}", e.getMessage(), e);
                throw new EmailSendingException("Failed to send email", e);
            }
        }, () -> log.warn("FileUpload with id {} not found. No email will be sent.", uploadId));
    }
}