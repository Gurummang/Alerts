package com.GASB.alerts.service;

import com.GASB.alerts.exception.AlertSettingsNotFoundException;
import com.GASB.alerts.model.entity.AlertSettings;
import com.GASB.alerts.model.entity.FileUpload;
import com.GASB.alerts.model.entity.StoredFile;
import com.GASB.alerts.model.entity.VtReport;
import com.GASB.alerts.repository.*;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class AwsMailService{

    private final AmazonSimpleEmailService amazonSimpleEmailService;
    private final AlertSettingsRepo alertSettingsRepo;
    private final FileUploadRepo fileUploadRepo;
    private final StoredFileRepo storedFileRepo;
    private final AlertEmailsRepo alertEmailsRepo;

    @RabbitListener(queues = "#{@rabbitMQProperties.vtQueue}")
    public void send(String payload) {
        long uploadId = Long.parseLong(payload.trim());
        System.out.println("uploadId: "+ uploadId);
        Long orgId = getOrgIdByUploadId(uploadId);
        List<AlertSettings> alertSettings = alertSettingsRepo.findAllByOrgIdAndVtTrue(orgId);

        // storedFile에 이미 존재하면서 vtReport에서 악성으로 판별난 경우
        if (isExistedStoredFile(uploadId) && isMalware(uploadId)) {
            sendMail(alertSettings);
        } else {
            System.out.println("전혀 위험하지 않음");
        }
    }

    private Long getOrgIdByUploadId(long uploadId) {
        return Optional.ofNullable(fileUploadRepo.findOrgIdByUploadId(uploadId))
                .orElseThrow(() -> new AlertSettingsNotFoundException("Organization ID not found for uploadId: " + uploadId));
    }

    private boolean isExistedStoredFile(long uploadId) {
        FileUpload fileUpload = getFileUploadById(uploadId);
        return storedFileRepo.existsBySaltedHash(fileUpload.getHash());
    }

    private boolean isMalware(long uploadId) {
        StoredFile storedFile = getStoredFileByUploadId(uploadId);
        VtReport vtReport = storedFile.getVtReport();
        return vtReport != null && !"none".equals(vtReport.getThreatLabel());
    }

    private FileUpload getFileUploadById(long uploadId) {
        return fileUploadRepo.findById(uploadId)
                .orElseThrow(() -> new AlertSettingsNotFoundException("FileUpload not found for id: " + uploadId));
    }

    private StoredFile getStoredFileByUploadId(long uploadId) {
        return Optional.ofNullable(getFileUploadById(uploadId).getStoredFile())
                .orElseThrow(() -> new AlertSettingsNotFoundException("StoredFile not found for uploadId: " + uploadId));
    }

    private void sendMail(List<AlertSettings> alertSettings){
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
