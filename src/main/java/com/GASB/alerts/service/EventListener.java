package com.GASB.alerts.service;

import com.GASB.alerts.config.RabbitMQProperties;
import com.GASB.alerts.exception.AlertSettingsNotFoundException;
import com.GASB.alerts.model.entity.*;
import com.GASB.alerts.repository.AlertSettingsRepo;
import com.GASB.alerts.repository.FileUploadRepo;
import com.GASB.alerts.repository.StoredFileRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
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
    public void uploadEvent(String payload) {
        long uploadId = Long.parseLong(payload.trim());
        log.info("uploadId: " + uploadId);

        StoredFile storedFile = getStoredFile(uploadId);

        if (storedFile.getFileStatus().getVtStatus() == 1 ||
                storedFile.getFileStatus().getDlpStatus() == 1 ||
                storedFile.getFileStatus().getGscanStatus() == 1) {

            // 조건에 맞는 알림 설정을 확인하고 메일 전송
            sendMailBasedOnConditions(uploadId);
        }
    }

    private void sendMailBasedOnConditions(long uploadId) {
        Set<String> notificationTypes = determineNotificationTypes(uploadId);
        Long orgId = getOrgIdByUploadId(uploadId);

        if (notificationTypes.isEmpty()) {
            log.info("No matching conditions for uploadId: " + uploadId);
            return;
        }

        // 모든 알림 설정을 가져옵니다
        List<AlertSettings> allAlertSettings = alertSettingsRepo.findAllByOrgId(orgId);
        Set<Long> sentAlertSettings = new HashSet<>();

        // 조건별로 메일 전송
        for (String type : notificationTypes) {
            List<AlertSettings> matchedSettings = allAlertSettings.stream()
                    .filter(setting -> {
                        switch (type) {
                            case "vt":
                                return setting.isVt();
                            case "dlp":
                                return setting.isDlp();
                            case "suspicious":
                                return setting.isSuspicious();
                            default:
                                return false;
                        }
                    })
                    .toList();

            // 이미 보낸 알림인지 체크하고 전송할 알림 필터링
            List<AlertSettings> filteredSettings = matchedSettings.stream()
                    .filter(setting -> !sentAlertSettings.contains(setting.getId()))
                    .toList();

            if (!filteredSettings.isEmpty()) {
                awsMailService.sendMail(filteredSettings);

                filteredSettings.forEach(setting -> sentAlertSettings.add(setting.getId()));
            }
        }

        sentAlertSettings.clear();
    }

    private Set<String> determineNotificationTypes(long uploadId) {
        Set<String> notificationTypes = new HashSet<>();

        if (isMalware(uploadId)) {
            notificationTypes.add("vt");
        }
        if (isSensitive(uploadId)) {
            notificationTypes.add("dlp");
        }
        if (isSuspicious(uploadId)) {
            notificationTypes.add("suspicious");
        }

        return notificationTypes;
    }


    @RabbitListener(queues = "#{@rabbitMQProperties.vtQueue}")
    public void vtEvent(long uploadId) {
        System.out.println("uploadId: " + uploadId);
        Long orgId = getOrgIdByUploadId(uploadId);
        List<AlertSettings> alertSettings = alertSettingsRepo.findAllByOrgIdAndVtTrue(orgId);

        // VT 상태와 일치하는 알림 설정에 따라 메일 전송
        sendMailIfConditionsMatch(alertSettings, uploadId, "vt");
    }

    @RabbitListener(queues = "#{@rabbitMQProperties.suspiciousQueue}")
    public void suspiciousEvent(long uploadId) {
        System.out.println("uploadId: " + uploadId);
        Long orgId = getOrgIdByUploadId(uploadId);
        List<AlertSettings> alertSettings = alertSettingsRepo.findAllByOrgIdAndSuspiciousTrue(orgId);

        // Suspicious 상태와 일치하는 알림 설정에 따라 메일 전송
        sendMailIfConditionsMatch(alertSettings, uploadId, "suspicious");
    }

    @RabbitListener(queues = "#{@rabbitMQProperties.dlpQueue}")
    public void dlpEvent(long uploadId) {
        System.out.println("uploadId: " + uploadId);
        Long orgId = getOrgIdByUploadId(uploadId);
        List<AlertSettings> alertSettings = alertSettingsRepo.findAllByOrgIdAndDlpTrue(orgId);

        // DLP 상태와 일치하는 알림 설정에 따라 메일 전송
        sendMailIfConditionsMatch(alertSettings, uploadId, "dlp");
    }

    private void sendMailIfConditionsMatch(List<AlertSettings> alertSettings, long uploadId, String alertType) {
        List<AlertSettings> matchedSettings = alertSettings.stream()
                .filter(setting -> {
                    switch (alertType) {
                        case "vt":
                            System.out.println("vt");
                            return setting.isVt();  // VT 상태와 일치하는지 확인
                        case "suspicious":
                            System.out.println("suspicious");
                            return setting.isSuspicious();  // Suspicious 상태와 일치하는지 확인
                        case "dlp":
                            System.out.println("dlp");
                            return setting.isDlp();  // DLP 상태와 일치하는지 확인
                        default:
                            return false;
                    }
                })
                .toList();

        // 조건을 만족하는 알림 설정이 있는 경우에만 메일 전송
        if (!matchedSettings.isEmpty()) {
            awsMailService.sendMail(matchedSettings);
        } else {
            log.info("No matching alert settings found for alert type: " + alertType + " and upload ID: " + uploadId);
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

        if(fileUpload.isPresent()){
            FileUpload upload = fileUpload.get();
            StoredFile storedFile = upload.getStoredFile();
            return upload.getTypeScan().getCorrect().equals(false) || storedFile.getScanTable().isDetected();
        }
        return false;
    }

    private boolean isSensitive(long uploadId) {
        return fileUploadRepo.findById(uploadId)
                .map(FileUpload::getStoredFile)
                .filter(storedFile -> storedFile.getDlpReport() != null) // DlpReport가 null이 아닌지 확인
                .map(storedFile -> storedFile.getDlpReport().stream()
                        .anyMatch(dlp -> dlp.getInfoCnt() >= 1)) // infoCnt가 1 이상인 DlpReport가 있는지 확인
                .orElse(false); // 기본값은 false
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
