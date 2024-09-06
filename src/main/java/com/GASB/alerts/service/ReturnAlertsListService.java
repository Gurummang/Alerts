package com.GASB.alerts.service;

import com.GASB.alerts.model.dto.response.AlertsListResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReturnAlertsListService {

    public AlertsListResponse getAlerts(long adminId, long alertsId){
        return AlertsListResponse.builder()
                .id(alertsId)
                .email("")
                .title("")
                .content("")
                .suspicious(true)
                .sensitive(false)
                .vt(true)
                .build();
    }

    public List<AlertsListResponse> getAlertsList(long adminId){
        ArrayList<AlertsListResponse> list = new ArrayList<>();

        AlertsListResponse ex = new AlertsListResponse(1,"", "", "", true, false, true);
        list.add(ex);

        return list;
    }
}
