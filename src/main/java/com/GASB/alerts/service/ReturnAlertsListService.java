package com.GASB.alerts.service;

import com.GASB.alerts.exception.AlertSettingsNotFoundException;
import com.GASB.alerts.exception.UnauthorizedAccessException;
import com.GASB.alerts.model.dto.response.AlertsListResponse;
import com.GASB.alerts.model.entity.AlertEmails;
import com.GASB.alerts.model.entity.AlertSettings;
import com.GASB.alerts.repository.AlertEmailsRepo;
import com.GASB.alerts.repository.AlertSettingsRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReturnAlertsListService {

    private final AlertSettingsRepo alertSettingsRepo;
    private final AlertEmailsRepo alertEmailsRepo;

    public ReturnAlertsListService(AlertSettingsRepo alertSettingsRepo, AlertEmailsRepo alertEmailsRepo){
        this.alertSettingsRepo = alertSettingsRepo;
        this.alertEmailsRepo = alertEmailsRepo;
    }

    public AlertsListResponse getAlerts(long orgId, long alertsId) {

        Optional<AlertSettings> alertSettingsOptional = alertSettingsRepo.findById(alertsId);

        if (alertSettingsOptional.isEmpty()) {
            throw new AlertSettingsNotFoundException("No AlertSettings found for Id: " + alertsId);
        }

        AlertSettings alertSettings = alertSettingsOptional.get();

        if (alertSettings.getAdminUsers().getOrg().getId() != orgId) {
            throw new UnauthorizedAccessException("Admin is not authorized to update alertSettings with id: " + alertsId);
        }

        return AlertsListResponse.builder()
                .id(alertsId)
                .email(getEmail(alertsId))
                .title(alertSettings.getTitle())
                .content(alertSettings.getContent())
                .suspicious(alertSettings.isSuspicious())
                .sensitive(alertSettings.isDlp())
                .vt(alertSettings.isVt())
                .build();
    }


    private List<String> getEmail(long alertId){
        return alertEmailsRepo.findEmailByAlertId(alertId);
    }

    public List<AlertsListResponse> getAlertsList(long orgId){
        List<AlertSettings> settings = alertSettingsRepo.findAllByOrgId(orgId);

        return settings.stream().map(setting -> {
            // AlertEmails 엔티티 리스트에서 이메일 주소 추출
            List<String> emails = setting.getAlertEmails().stream()
                    .map(AlertEmails::getEmail)
                    .toList();

            // AlertsListResponse 객체 생성
            return AlertsListResponse.builder()
                    .id(setting.getId())
                    .email(emails)
                    .title(setting.getTitle())
                    .content(setting.getContent())
                    .suspicious(setting.isSuspicious())
                    .sensitive(setting.isDlp())
                    .vt(setting.isVt())
                    .build();
        }).toList();
    }
}
