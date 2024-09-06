package com.GASB.alerts.service;

import com.GASB.alerts.model.dto.request.SetEmailRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetEmailAlertsService {

    public String setAlerts(long adminId, SetEmailRequest setEmailRequest){
        return "success to set alerts.";
    }

    public String updateAlerts(long adminId, SetEmailRequest setEmailRequest){
        return "success to modify alerts.";
    }

    public String deleteAlerts(long adminId, List<Long> alertIds){
        return "success to delete alerts.";
    }
}
