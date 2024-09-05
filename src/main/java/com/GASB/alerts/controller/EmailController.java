package com.GASB.alerts.controller;

import com.GASB.alerts.annotation.JWT.ValidateJWT;
import com.GASB.alerts.model.dto.request.DeleteAlertsRequest;
import com.GASB.alerts.model.dto.request.SetEmailRequest;
import com.GASB.alerts.model.dto.response.AlertsListResponse;
import com.GASB.alerts.model.dto.response.ResponseDto;
import com.GASB.alerts.model.entity.AdminUsers;
import com.GASB.alerts.repository.AdminUsersRepo;
import com.GASB.alerts.service.ReturnAlertsListService;
import com.GASB.alerts.service.SetEmailAlertsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/alerts")
public class EmailController {

    private static final String ERROR = "error";
    private static final String EMAIL = "email";
    private static final String EMAIL_NOT_FOUND = "Admin not found with email: ";
    private static final String INVALID_JWT_MSG = "Invalid JWT: email attribute is missing.";
    private static final String ID_IS_NULL = "Id id null.";
    private final AdminUsersRepo adminUsersRepo;

    private final SetEmailAlertsService setEmailAlertsService;
    private final ReturnAlertsListService returnAlertsListService;

    public EmailController(AdminUsersRepo adminUsersRepo, SetEmailAlertsService setEmailAlertsService, ReturnAlertsListService returnAlertsListService){
        this.adminUsersRepo = adminUsersRepo;
        this.setEmailAlertsService = setEmailAlertsService;
        this.returnAlertsListService = returnAlertsListService;
    }

    // 알림 설정 리스트 가져오기
    @GetMapping("/email")
    @ValidateJWT
    public ResponseDto<List<AlertsListResponse>> getAlertsList(HttpServletRequest servletRequest){
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

            long adminId = adminOptional.get().getId();
            List<AlertsListResponse> result = returnAlertsListService.getAlertsList(adminId);
            return ResponseDto.ofSuccess(result);
        } catch (Exception e){
            return ResponseDto.ofFail(e.getMessage());
        }
    }

    // 알림 설정 저장
    @PostMapping("/email")
    @ValidateJWT
    public ResponseDto<String> setEmailAlerts(HttpServletRequest servletRequest, @RequestBody SetEmailRequest setEmailRequest){
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

            long adminId = adminOptional.get().getId();
            String save = setEmailAlertsService.setAlerts(adminId, setEmailRequest);
            return ResponseDto.ofSuccess(save);
        } catch (Exception e){
            return ResponseDto.ofFail(e.getMessage());
        }
    }

    // 알림 설정 하나만 조회
    @GetMapping("/email/{id}")
    @ValidateJWT
    public ResponseDto<AlertsListResponse> getEmailAlerts(HttpServletRequest servletRequest, @PathVariable Long id){
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

            if(id == null){
                return ResponseDto.ofFail(ID_IS_NULL);
            }

            long adminId = adminOptional.get().getId();
            AlertsListResponse result = returnAlertsListService.getAlerts(adminId, id);
            return ResponseDto.ofSuccess(result);
        } catch (Exception e){
            return ResponseDto.ofFail(e.getMessage());
        }
    }

    // 알림 설정 수정
    @PutMapping("/email/{id}")
    @ValidateJWT
    public ResponseDto<String> modifyEmailAlerts(HttpServletRequest servletRequest,@PathVariable Long id, @RequestBody SetEmailRequest setEmailRequest){
        try{
            if (servletRequest.getAttribute(ERROR) != null) {
                String errorMessage = (String) servletRequest.getAttribute(ERROR);
                return ResponseDto.ofFail(errorMessage);
            }
            String email = (String) servletRequest.getAttribute(EMAIL);

            if(email == null) {
                return ResponseDto.ofFail(INVALID_JWT_MSG);
            }

            Optional<AdminUsers> adminOptional = adminUsersRepo.findByEmail(email);
            if (adminOptional.isEmpty()) {
                return ResponseDto.ofFail(EMAIL_NOT_FOUND + email);
            }

            if(id == null){
                return ResponseDto.ofFail(ID_IS_NULL);
            }

            long adminId = adminOptional.get().getId();
            String modify = setEmailAlertsService.updateAlerts(adminId, setEmailRequest);
            return ResponseDto.ofSuccess(modify);
        } catch (Exception e){
            return ResponseDto.ofFail(e.getMessage());
        }
    }

    // 알림 설정 삭제
    @DeleteMapping("/email")
    @ValidateJWT
    public ResponseDto<String> deleteEmailAlerts(HttpServletRequest servletRequest, @RequestBody DeleteAlertsRequest deleteAlertsRequest){
        try{
            if (servletRequest.getAttribute(ERROR) != null) {
                String errorMessage = (String) servletRequest.getAttribute(ERROR);
                return ResponseDto.ofFail(errorMessage);
            }
            String email = (String) servletRequest.getAttribute(EMAIL);

            if(email == null) {
                return ResponseDto.ofFail(INVALID_JWT_MSG);
            }

            Optional<AdminUsers> adminOptional = adminUsersRepo.findByEmail(email);
            if (adminOptional.isEmpty()) {
                return ResponseDto.ofFail(EMAIL_NOT_FOUND + email);
            }

            long adminId = adminOptional.get().getId();
            String modify = setEmailAlertsService.deleteAlerts(adminId, deleteAlertsRequest.getAlertIds());
            return ResponseDto.ofSuccess(modify);
        } catch (Exception e){
            return ResponseDto.ofFail(e.getMessage());
        }
    }

}
