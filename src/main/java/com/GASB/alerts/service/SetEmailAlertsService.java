package com.GASB.alerts.service;

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
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found for id: " + adminId));

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
    public String updateAlerts(long alertId, SetEmailRequest setEmailRequest) {
        Optional<AlertSettings> alertSettingsOptional = alertSettingsRepo.findById(alertId);

        if(alertSettingsOptional.isPresent()){
            AlertSettings alertSettings = alertSettingsOptional.get();

            alertSettings.setTitle(setEmailRequest.getTitle());
            alertSettings.setContent(setEmailRequest.getContent());
            alertSettings.setSuspicious(setEmailRequest.isSuspicious());
            alertSettings.setDlp(setEmailRequest.isSensitive());
            alertSettings.setVt(setEmailRequest.isVt());

            List<AlertEmails> newAlertEmails = setEmailRequest.getEmail().stream()
                    .map(email -> AlertEmails.builder()
                            .alertSettings(alertSettings)
                            .email(email)
                            .build())
                    .collect(Collectors.toList());

            // 기존 이메일 삭제 후 새 이메일 저장
            alertEmailsRepo.deleteByAlertSettings(alertSettings);
            alertEmailsRepo.saveAll(newAlertEmails);

            alertSettingsRepo.save(alertSettings);
            return "success to modify alerts.";
        }else{
            throw new RuntimeException("AlertEmails 엔티티를 찾을 수 없습니다.");
        }
    }

    @Transactional
    public String deleteAlerts(long adminId, List<Long> alertIds) {
        AdminUsers adminUsers = adminUsersRepo.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found for id: " + adminId));

        List<AlertSettings> alertSettingsList = alertSettingsRepo.findAllById(alertIds);
        for (AlertSettings alertSettings : alertSettingsList) {
            if (alertSettings.getAdminUsers().equals(adminUsers)) {
                alertSettingsRepo.delete(alertSettings);
            }
        }
        return "success to delete alerts.";
    }
}
