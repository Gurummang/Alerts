package com.GASB.alerts.service;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.VerifyEmailIdentityRequest;
import com.amazonaws.services.simpleemail.model.VerifyEmailIdentityResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailVerificationService {

    private final AmazonSimpleEmailService amazonSimpleEmailService;

    @Autowired
    public EmailVerificationService(AmazonSimpleEmailService amazonSimpleEmailService) {
        this.amazonSimpleEmailService = amazonSimpleEmailService;
    }

    public String verifyEmail(String emailAddress) {
        // 이메일 검증 요청 생성
        VerifyEmailIdentityRequest request = new VerifyEmailIdentityRequest()
                .withEmailAddress(emailAddress);

        // AWS SES 클라이언트를 사용하여 이메일 검증 요청
        VerifyEmailIdentityResult response = amazonSimpleEmailService.verifyEmailIdentity(request);

        // 결과 반환
        return "Verification email sent to " + emailAddress;
    }
}

