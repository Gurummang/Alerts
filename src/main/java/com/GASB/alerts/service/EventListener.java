package com.GASB.alerts.service;

import com.GASB.alerts.config.RabbitMQProperties;
import com.GASB.alerts.exception.AlertSettingsNotFoundException;
import com.GASB.alerts.model.entity.*;
import com.GASB.alerts.repository.AlertSettingsRepo;
import com.GASB.alerts.repository.FileUploadRepo;
import com.GASB.alerts.repository.StoredFileRepo;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventListener {

    private final AlertSettingsRepo alertSettingsRepo;
    private final FileUploadRepo fileUploadRepo;
    private final StoredFileRepo storedFileRepo;
    private final AwsMailService awsMailService;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties rabbitMQProperties;

    public EventListener(AlertSettingsRepo alertSettingsRepo, FileUploadRepo fileUploadRepo, StoredFileRepo storedFileRepo, AwsMailService awsMailService, RabbitTemplate rabbitTemplate, RabbitMQProperties rabbitMQProperties){
        this.alertSettingsRepo = alertSettingsRepo;
        this.fileUploadRepo = fileUploadRepo;
        this.storedFileRepo = storedFileRepo;
        this.awsMailService = awsMailService;
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitMQProperties = rabbitMQProperties;
    }

    // params: 업로드 id
    // storedFile이 이미 있는 경우
    @RabbitListener(queues = "#{@rabbitMQProperties.uploadQueue}")
    public void uploadEvent(String payload){
        long uploadId = Long.parseLong(payload.trim());
        System.out.println("uploadId: "+ uploadId);

        StoredFile storedFile = getStoredFile(uploadId);

        if(storedFile.getFileStatus().getVtStatus() == 1) {
            rabbitTemplate.convertAndSend(rabbitMQProperties.getVtRoutingKey(), uploadId);
        }

        if(storedFile.getFileStatus().getDlpStatus() == 1){
            rabbitTemplate.convertAndSend(rabbitMQProperties.getDlpRoutingKey(), uploadId);
        }

        if(storedFile.getFileStatus().getGscanStatus() == 1){
            rabbitTemplate.convertAndSend(rabbitMQProperties.getSuspiciousRoutingKey(), uploadId);
        }
    }

    @RabbitListener(queues = "#{@rabbitMQProperties.vtQueue}")
    public void vtEvent(long uploadId) {
        System.out.println("uploadId: "+ uploadId);
        Long orgId = getOrgIdByUploadId(uploadId);
        List<AlertSettings> alertSettings = alertSettingsRepo.findAllByOrgIdAndVtTrue(orgId);

        // storedFile에 이미 존재하면서 vtReport에서 악성으로 판별난 경우
        if (isMalware(uploadId)) {
            awsMailService.sendMail(alertSettings);
        } else {
            System.out.println("전혀 위험하지 않음");
        }
    }

    @RabbitListener(queues = "#{@rabbitMQProperties.suspiciousQueue}")
    public void suspiciousEvent(long uploadId){
        System.out.println("uploadId: "+ uploadId);
        Long orgId = getOrgIdByUploadId(uploadId);
        List<AlertSettings> alertSettings = alertSettingsRepo.findAllByOrgIdAndSuspiciousTrue(orgId);

        // storedFile에 이미 존재하면서 gscan에서 의심으로 판별난 경우
        if(isSuspicious(uploadId)) {
            awsMailService.sendMail(alertSettings);
        } else {
            System.out.println("의심스럽지 않음..");
        }
    }

    @RabbitListener(queues = "#{@rabbitMQProperties.dlpQueue}")
    public void dlpEvent(long uploadId){
        System.out.println("uploadId: "+ uploadId);
        Long orgId = getOrgIdByUploadId(uploadId);
        List<AlertSettings> alertSettings = alertSettingsRepo.findAllByOrgIdAndDlpTrue(orgId);

        // storedFile에 이미 존재하면서 dlpReport에서 민감으로 판별난 경우
        if(isSensitive(uploadId)) {
            awsMailService.sendMail(alertSettings);
        } else {
            System.out.println("dlp 감지 안됨...");
        }
    }

    private Long getOrgIdByUploadId(long uploadId) {
        return Optional.ofNullable(fileUploadRepo.findOrgIdByUploadId(uploadId))
                .orElseThrow(() -> new AlertSettingsNotFoundException("Organization ID not found for uploadId: " + uploadId));
    }

    private StoredFile getStoredFile(long uploadId){
        FileUpload fileUpload = getFileUploadById(uploadId);
        return storedFileRepo.findBySaltedHash(fileUpload.getHash());
    }

    private boolean isMalware(long uploadId) {
        StoredFile storedFile = getStoredFileByUploadId(uploadId);
        VtReport vtReport = storedFile.getVtReport();
        return vtReport != null && !"none".equals(vtReport.getThreatLabel());
    }

    private boolean isSuspicious(long uploadId){
        Optional<FileUpload> fileUpload = fileUploadRepo.findById(uploadId);
        StoredFile storedFile = fileUpload.get().getStoredFile();
        return fileUpload.get().getTypeScan().getCorrect().equals(false) || storedFile.getScanTable().isDetected();
    }

    private boolean isSensitive(long uploadId){
        Optional<FileUpload> fileUpload = fileUploadRepo.findById(uploadId);
        DlpReport dlpReport = fileUpload.get().getStoredFile().getDlpReport();
        return dlpReport.getDlp().equals(true);
    }

    private FileUpload getFileUploadById(long uploadId) {
        return fileUploadRepo.findById(uploadId)
                .orElseThrow(() -> new AlertSettingsNotFoundException("FileUpload not found for id: " + uploadId));
    }

    private StoredFile getStoredFileByUploadId(long uploadId) {
        return Optional.ofNullable(getFileUploadById(uploadId).getStoredFile())
                .orElseThrow(() -> new AlertSettingsNotFoundException("StoredFile not found for uploadId: " + uploadId));
    }
}
