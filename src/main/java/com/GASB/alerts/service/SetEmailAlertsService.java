package com.GASB.alerts.service;

import com.GASB.alerts.exception.AlertSettingsNotFoundException;
import com.GASB.alerts.exception.UnauthorizedAccessException;
import com.GASB.alerts.model.dto.request.SetEmailRequest;
import com.GASB.alerts.model.dto.response.SetEmailsResponse;
import com.GASB.alerts.model.entity.AdminUsers;
import com.GASB.alerts.model.entity.AlertEmails;
import com.GASB.alerts.model.entity.AlertSettings;
import com.GASB.alerts.repository.AdminUsersRepo;
import com.GASB.alerts.repository.AlertEmailsRepo;
import com.GASB.alerts.repository.AlertSettingsRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service public class SetEmailAlertsService {

    private final AdminUsersRepo adminUsersRepo;
    private final AlertSettingsRepo alertSettingsRepo;
    private final AlertEmailsRepo alertEmailsRepo;

    private final EmailVerificationService emailVerificationService;

    public SetEmailAlertsService(AdminUsersRepo adminUsersRepo, AlertSettingsRepo alertSettingsRepo, AlertEmailsRepo alertEmailsRepo, EmailVerificationService emailVerificationService){
        this.adminUsersRepo = adminUsersRepo;
        this.alertSettingsRepo = alertSettingsRepo;
        this.alertEmailsRepo = alertEmailsRepo;
        this.emailVerificationService=emailVerificationService;
    }

    @Transactional
    public SetEmailsResponse setAlerts(long adminId, SetEmailRequest setEmailRequest){
        AdminUsers adminUsers = adminUsersRepo.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));
        List<String> emails = setEmailRequest.getEmail();
        List<String> unverifiedEmails = new ArrayList<>();

        // 이메일 검증
        for (String email : emails) {
            boolean isVerified = emailVerificationService.isVerified(email);
            if (!isVerified) {
                unverifiedEmails.add(email);
            }
        }

        if (unverifiedEmails.isEmpty()) {
            // 모든 이메일이 검증된 경우 알람 설정 저장
            AlertSettings alertSettings = AlertSettings.toEntity(setEmailRequest, adminUsers);

            List<AlertEmails> alertEmails = emails.stream()
                    .map(email -> AlertEmails.builder()
                            .email(email)
                            .alertSettings(alertSettings)
                            .build())
                    .toList();

            alertSettings.setAlertEmails(alertEmails);
            alertSettingsRepo.save(alertSettings);
            return new SetEmailsResponse("알람 설정이 완료되었습니다.", unverifiedEmails);
        } else {
            return new SetEmailsResponse("등록되지 않은 이메일이 있습니다.인증 메일을 보내시겠습니까?", unverifiedEmails);
        }
    }

    @Transactional
    public SetEmailsResponse updateAlerts(long orgId, long alertId, SetEmailRequest setEmailRequest) {
        // AlertSettings 조회
        Optional<AlertSettings> alertSettingsOptional = alertSettingsRepo.findById(alertId);

        if (alertSettingsOptional.isEmpty()) {
            throw new AlertSettingsNotFoundException("AlertSettings not found for id: " + alertId);
        }

        AlertSettings alertSettings = alertSettingsOptional.get();

        if (alertSettings.getAdminUsers().getOrg().getId() != orgId) {
            throw new UnauthorizedAccessException("Admin is not authorized to update alertSettings with id: " + alertId);
        }

        List<String> emails = setEmailRequest.getEmail();
        List<String> unverifiedEmails = new ArrayList<>();

        // 이메일 검증
        for (String email : emails) {
            boolean isVerified = emailVerificationService.isVerified(email);
            if (!isVerified) {
                unverifiedEmails.add(email);
            }
        }

        if (unverifiedEmails.isEmpty()) {
            // 모든 이메일이 검증된 경우 알람 설정 저장
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
            return new SetEmailsResponse("Successfully modified alerts.", unverifiedEmails);
        } else {
            return new SetEmailsResponse("등록되지 않은 이메일이 있습니다.인증 메일을 보내시겠습니까?", unverifiedEmails);
        }
    }


    @Transactional
    public String deleteAlerts(long orgId, List<Long> alertIds) {
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
            if (alertSettings.getAdminUsers().getOrg().getId() == orgId) {
                alertSettingsRepo.delete(alertSettings);
            } else {
                throw new UnauthorizedAccessException("Admin is not authorized to delete alertSettings with id: " + alertSettings.getId());
            }
        }

        return "success to delete alerts.";
    }
}
