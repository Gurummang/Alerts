package com.GASB.alerts.service;

import com.GASB.alerts.model.dto.SetEmailRequest;
import org.springframework.stereotype.Service;

@Service
public class SetEmailAlertsService {

    public String setAlerts(long orgId, SetEmailRequest setEmailRequest){
        return "success to set alerts.";
    }
}
