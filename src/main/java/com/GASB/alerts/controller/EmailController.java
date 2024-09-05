package com.GASB.alerts.controller;

import com.GASB.alerts.annotation.JWT.ValidateJWT;
import com.GASB.alerts.model.dto.SetEmailRequest;
import com.GASB.alerts.model.dto.ResponseDto;
import com.GASB.alerts.model.entity.AdminUsers;
import com.GASB.alerts.repository.AdminUsersRepo;
import com.GASB.alerts.service.SetEmailAlertsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/email")
public class EmailController {

    private static final String ERROR = "error";
    private static final String EMAIL = "email";
    private static final String EMAIL_NOT_FOUND = "Admin not found with email: ";
    private static final String INVALID_JWT_MSG = "Invalid JWT: email attribute is missing.";

    private final AdminUsersRepo adminUsersRepo;

    private final SetEmailAlertsService setEmailAlertsService;

    public EmailController(AdminUsersRepo adminUsersRepo, SetEmailAlertsService setEmailAlertsService){
        this.adminUsersRepo = adminUsersRepo;
        this.setEmailAlertsService = setEmailAlertsService;
    }

    @PostMapping("/set")
    @ValidateJWT
    public ResponseDto<String> setEmailAlerts(HttpServletRequest servletRequest, SetEmailRequest setEmailRequest){
        try {
            if (servletRequest.getAttribute(ERROR) != null) {
                String errorMessage = (String) servletRequest.getAttribute(ERROR);
                return ResponseDto.ofFail(errorMessage);
            }
            String email = (String) servletRequest.getAttribute(EMAIL);

            if (email == null) {
                return ResponseDto.ofFail(INVALID_JWT_MSG);
            }

            Optional<AdminUsers> adminOptional = adminUsersRepo.findByEmail(email);
            if (adminOptional.isEmpty()) {
                return ResponseDto.ofFail(EMAIL_NOT_FOUND + email);
            }

            long orgId = adminOptional.get().getOrg().getId();
            String save = setEmailAlertsService.setAlerts(orgId, setEmailRequest);
            return ResponseDto.ofSuccess(save);
        } catch (Exception e){
            return ResponseDto.ofFail(e.getMessage());
        }
    }
}
