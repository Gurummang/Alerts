package com.GASB.alerts.controller;

import com.GASB.alerts.annotation.JWT.ValidateJWT;
import com.GASB.alerts.exception.InvalidJwtException;
import com.GASB.alerts.model.dto.request.DeleteAlertsRequest;
import com.GASB.alerts.model.dto.request.SetEmailRequest;
import com.GASB.alerts.model.dto.response.AlertsListResponse;
import com.GASB.alerts.model.dto.response.ResponseDto;
import com.GASB.alerts.model.entity.AdminUsers;
import com.GASB.alerts.repository.AdminUsersRepo;
import com.GASB.alerts.service.AwsMailService;
import com.GASB.alerts.service.ReturnAlertsListService;
import com.GASB.alerts.service.SetEmailAlertsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/alerts")
public class EmailController {

    private static final String EMAIL = "email";
    private static final String EMAIL_NOT_FOUND = "Admin not found with email: ";
    private static final String INVALID_JWT_MSG = "Invalid JWT: email attribute is missing.";
    private final AdminUsersRepo adminUsersRepo;

    private final SetEmailAlertsService setEmailAlertsService;
    private final ReturnAlertsListService returnAlertsListService;
    private final AwsMailService awsMailService;

    public EmailController(AdminUsersRepo adminUsersRepo, SetEmailAlertsService setEmailAlertsService, ReturnAlertsListService returnAlertsListService, AwsMailService awsMailService){
        this.adminUsersRepo = adminUsersRepo;
        this.setEmailAlertsService = setEmailAlertsService;
        this.returnAlertsListService = returnAlertsListService;
        this.awsMailService = awsMailService;
    }

    private Optional<AdminUsers> getAdminUser(HttpServletRequest servletRequest) {
        String email = (String) servletRequest.getAttribute(EMAIL);
        if (email == null) {
            throw new InvalidJwtException(INVALID_JWT_MSG);
        }
        return adminUsersRepo.findByEmail(email);
    }

    // 알림 설정 리스트 가져오기
    @GetMapping
    @ValidateJWT
    public ResponseDto<List<AlertsListResponse>> getAlertsList(HttpServletRequest servletRequest){
        try {
            Optional<AdminUsers> adminOptional = getAdminUser(servletRequest);
            if (adminOptional.isEmpty()) {
                return ResponseDto.ofFail(EMAIL_NOT_FOUND);
            }

            long adminId = adminOptional.get().getId();
            List<AlertsListResponse> result = returnAlertsListService.getAlertsList(adminId);
            return ResponseDto.ofSuccess(result);
        } catch (Exception e){
            return ResponseDto.ofFail(e.getMessage());
        }
    }

    // 알림 설정 저장
    @PostMapping
    @ValidateJWT
    public ResponseDto<String> setEmailAlerts(HttpServletRequest servletRequest, @RequestBody SetEmailRequest setEmailRequest){
        try {
            Optional<AdminUsers> adminOptional = getAdminUser(servletRequest);
            if (adminOptional.isEmpty()) {
                return ResponseDto.ofFail(EMAIL_NOT_FOUND);
            }

            long adminId = adminOptional.get().getId();
            String save = setEmailAlertsService.setAlerts(adminId, setEmailRequest);
            return ResponseDto.ofSuccess(save);
        } catch (Exception e){
            return ResponseDto.ofFail(e.getMessage());
        }
    }

    // 알림 설정 하나만 조회
    @GetMapping("/edit/{id}")
    @ValidateJWT
    public ResponseDto<AlertsListResponse> getEmailAlerts(HttpServletRequest servletRequest, @PathVariable Long id){
        try {
            Optional<AdminUsers> adminOptional = getAdminUser(servletRequest);
            if (adminOptional.isEmpty()) {
                return ResponseDto.ofFail(EMAIL_NOT_FOUND);
            }

            long adminId = adminOptional.get().getId();
            AlertsListResponse result = returnAlertsListService.getAlerts(adminId, id);
            return ResponseDto.ofSuccess(result);
        } catch (Exception e){
            return ResponseDto.ofFail(e.getMessage());
        }
    }

    // 알림 설정 수정
    @PutMapping("/edit/{id}")
    @ValidateJWT
    public ResponseDto<String> modifyEmailAlerts(HttpServletRequest servletRequest,@PathVariable long id, @RequestBody SetEmailRequest setEmailRequest){
        try{
            Optional<AdminUsers> adminOptional = getAdminUser(servletRequest);
            if (adminOptional.isEmpty()) {
                return ResponseDto.ofFail(EMAIL_NOT_FOUND);
            }

            long adminId = adminOptional.get().getId();
            String modify = setEmailAlertsService.updateAlerts(adminId, id, setEmailRequest);
            return ResponseDto.ofSuccess(modify);
        } catch (Exception e){
            return ResponseDto.ofFail(e.getMessage());
        }
    }

    // 알림 설정 삭제
    @PostMapping("/delete")
    @ValidateJWT
    public ResponseDto<String> deleteEmailAlerts(HttpServletRequest servletRequest, @RequestBody DeleteAlertsRequest deleteAlertsRequest){
        try{
            Optional<AdminUsers> adminOptional = getAdminUser(servletRequest);
            if (adminOptional.isEmpty()) {
                return ResponseDto.ofFail(EMAIL_NOT_FOUND);
            }

            long adminId = adminOptional.get().getId();
            String delete = setEmailAlertsService.deleteAlerts(adminId, deleteAlertsRequest.getAlertIds());
            return ResponseDto.ofSuccess(delete);
        } catch (Exception e){
            return ResponseDto.ofFail(e.getMessage());
        }
    }

//    @GetMapping
//    public ResponseDto<String> sendMail(){
//        awsMailService.send();
//        return ResponseDto.ofSuccess("good!");
//    }

}
