package com.GASB.alerts.service;

import com.GASB.alerts.exception.AlertSettingsNotFoundException;
import com.GASB.alerts.model.entity.AlertSettings;
import com.GASB.alerts.model.entity.FileUpload;
import com.GASB.alerts.model.entity.StoredFile;
import com.GASB.alerts.model.entity.VtReport;
import com.GASB.alerts.repository.AlertSettingsRepo;
import com.GASB.alerts.repository.FileUploadRepo;
import com.GASB.alerts.repository.StoredFileRepo;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventListener {

    private final AlertSettingsRepo alertSettingsRepo;
    private final FileUploadRepo fileUploadRepo;
    private final StoredFileRepo storedFileRepo;
    private final AwsMailService awsMailService;

    public EventListener(AlertSettingsRepo alertSettingsRepo, FileUploadRepo fileUploadRepo, StoredFileRepo storedFileRepo, AwsMailService awsMailService){
        this.alertSettingsRepo = alertSettingsRepo;
        this.fileUploadRepo = fileUploadRepo;
        this.storedFileRepo = storedFileRepo;
        this.awsMailService = awsMailService;
    }

    @RabbitListener(queues = "#{@rabbitMQProperties.vtQueue}")
    public void vtEvent(String payload) {
        long uploadId = Long.parseLong(payload.trim());
        System.out.println("uploadId: "+ uploadId);
        Long orgId = getOrgIdByUploadId(uploadId);
        List<AlertSettings> alertSettings = alertSettingsRepo.findAllByOrgIdAndVtTrue(orgId);

        // storedFile에 이미 존재하면서 vtReport에서 악성으로 판별난 경우
        if (isExistedStoredFile(uploadId) && isMalware(uploadId)) {
            awsMailService.sendMail(alertSettings);
        } else {
            System.out.println("전혀 위험하지 않음");
        }
    }

    @RabbitListener(queues = "#{@rabbitMQProperties.suspiciousQueue}")
    public void suspiciousEvent(String payload){
        long uploadId = Long.parseLong(payload.trim());
        System.out.println("uploadId: "+ uploadId);
        Long orgId = getOrgIdByUploadId(uploadId);
        List<AlertSettings> alertSettings = alertSettingsRepo.findAllByOrgIdAndSuspiciousTrue(orgId);

        awsMailService.sendMail(alertSettings);
    }

    @RabbitListener(queues = "#{@rabbitMQProperties.dlpQueue}")
    public void dlpEvent(String payload){
        long uploadId = Long.parseLong(payload.trim());
        System.out.println("uploadId: "+ uploadId);
        Long orgId = getOrgIdByUploadId(uploadId);
        List<AlertSettings> alertSettings = alertSettingsRepo.findAllByOrgIdAndDlpTrue(orgId);

        awsMailService.sendMail(alertSettings);
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
}
