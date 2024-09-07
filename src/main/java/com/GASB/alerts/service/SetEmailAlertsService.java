package com.GASB.alerts.service;

import com.GASB.alerts.exception.AlertSettingsNotFoundException;
import com.GASB.alerts.exception.UnauthorizedAccessException;
import com.GASB.alerts.model.dto.request.SetEmailRequest;
import com.GASB.alerts.model.entity.AdminUsers;
import com.GASB.alerts.model.entity.AlertEmails;
import com.GASB.alerts.model.entity.AlertSettings;
import com.GASB.alerts.repository.AdminUsersRepo;
import com.GASB.alerts.repository.AlertEmailsRepo;
import com.GASB.alerts.repository.AlertSettingsRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service public class SetEmailAlertsService {

    private final AdminUsersRepo adminUsersRepo;
    private final AlertSettingsRepo alertSettingsRepo;
    private final AlertEmailsRepo alertEmailsRepo;

    public SetEmailAlertsService(AdminUsersRepo adminUsersRepo, AlertSettingsRepo alertSettingsRepo, AlertEmailsRepo alertEmailsRepo){
        this.adminUsersRepo = adminUsersRepo;
        this.alertSettingsRepo = alertSettingsRepo;
        this.alertEmailsRepo = alertEmailsRepo;
    }

    @Transactional
    public String setAlerts(long adminId, SetEmailRequest setEmailRequest){
        AdminUsers adminUsers = adminUsersRepo.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));

        AlertSettings alertSettings = AlertSettings.toEntity(setEmailRequest, adminUsers);

        List<AlertEmails> emails = setEmailRequest.getEmail().stream()
                .map(email -> AlertEmails.builder()
                        .email(email)
                        .alertSettings(alertSettings) // AlertEmails 엔티티에 AlertSettings 참조 설정
                        .build())
                .toList();

        alertSettings.setAlertEmails(emails);

        alertSettingsRepo.save(alertSettings);
        return "success to set alerts.";
    }

    @Transactional
    public String updateAlerts(long adminId, long alertId, SetEmailRequest setEmailRequest) {
        // AlertSettings 조회
        Optional<AlertSettings> alertSettingsOptional = alertSettingsRepo.findById(alertId);

        if (alertSettingsOptional.isEmpty()) {
            throw new AlertSettingsNotFoundException("AlertSettings not found for id: " + alertId);
        }

        AlertSettings alertSettings = alertSettingsOptional.get();

        if (alertSettings.getAdminUsers().getId() != adminId) {
            throw new UnauthorizedAccessException("Admin is not authorized to update alertSettings with id: " + alertId);
        }

        alertSettings.setTitle(setEmailRequest.getTitle());
        alertSettings.setContent(setEmailRequest.getContent());
        alertSettings.setSuspicious(setEmailRequest.isSuspicious());
        alertSettings.setDlp(setEmailRequest.isSensitive());
        alertSettings.setVt(setEmailRequest.isVt());

        // 기존 이메일 삭제 후 새 이메일 저장
        List<AlertEmails> newAlertEmails = setEmailRequest.getEmail().stream()
                .map(email -> AlertEmails.builder()
                        .alertSettings(alertSettings)
                        .email(email)
                        .build())
                .toList();

        alertEmailsRepo.deleteByAlertSettings(alertSettings);
        alertEmailsRepo.saveAll(newAlertEmails);

        alertSettingsRepo.save(alertSettings);

        return "Successfully modified alerts.";
    }


    @Transactional
    public String deleteAlerts(long adminId, List<Long> alertIds) {
        List<AlertSettings> alertSettingsList = alertSettingsRepo.findAllById(alertIds);

        // 존재하지 않는 ID를 찾기 위한 로직
        List<Long> existingAlertIds = alertSettingsList.stream()
                .map(AlertSettings::getId)
                .toList();

        List<Long> nonExistentAlertIds = alertIds.stream()
                .filter(id -> !existingAlertIds.contains(id))
                .toList();

        if (!nonExistentAlertIds.isEmpty()) {
            throw new AlertSettingsNotFoundException("AlertSettings not found for ids: " + nonExistentAlertIds);
        }

        for (AlertSettings alertSettings : alertSettingsList) {
            if (alertSettings.getAdminUsers().getId().equals(adminId)) {
                alertSettingsRepo.delete(alertSettings);
            } else {
                throw new UnauthorizedAccessException("Admin is not authorized to delete alertSettings with id: " + alertSettings.getId());
            }
        }

        return "success to delete alerts.";
    }
}
