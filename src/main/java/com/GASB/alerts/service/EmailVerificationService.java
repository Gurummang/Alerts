package com.GASB.alerts.service;

import com.GASB.alerts.model.dto.response.SetEmailsResponse;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.ListIdentitiesRequest;
import com.amazonaws.services.simpleemail.model.ListIdentitiesResult;
import com.amazonaws.services.simpleemail.model.VerifyEmailIdentityRequest;
import com.amazonaws.services.simpleemail.model.VerifyEmailIdentityResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.regex.Pattern;

@Service
public class EmailVerificationService {

    Logger logger = LoggerFactory.getLogger(EmailVerificationService.class);

    private final AmazonSimpleEmailService amazonSimpleEmailService;

    @Autowired
    public EmailVerificationService(AmazonSimpleEmailService amazonSimpleEmailService) {
        this.amazonSimpleEmailService = amazonSimpleEmailService;
    }

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    // 이메일 형식 검증 메서드
    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean isVerified(String emailAddress) {

        // 이메일 목록 가져오기 로그
        logger.info("Fetching the list of verified email addresses...");

        // 이미 검증된 이메일 주소 목록을 가져오기 위한 요청 생성
        ListIdentitiesRequest listIdentitiesRequest = new ListIdentitiesRequest().withIdentityType("EmailAddress");
        ListIdentitiesResult listIdentitiesResult = amazonSimpleEmailService.listIdentities(listIdentitiesRequest);

        // 검증된 이메일 주소 목록 가져오기
        List<String> verifiedEmails = listIdentitiesResult.getIdentities();

        // 확인용 로그
        logger.info("List of verified emails: " + verifiedEmails);

        // 이메일이 이미 검증된 상태인지 확인
        boolean isVerified = verifiedEmails.stream().anyMatch(verifiedEmail -> verifiedEmail.equalsIgnoreCase(emailAddress.trim()));

        if (isVerified) {
            logger.info("Email " + emailAddress + " is verified.");
        } else {
            logger.info("Email " + emailAddress + " is not verified.");
        }

        return isVerified;
    }

    public SetEmailsResponse verifyEmails(List<String> emails) {
        List<String> failedEmails = new ArrayList<>();
        boolean allSuccess = true;

        for (String e : emails) {
            // 이메일 형식 검증
            if (!isValidEmail(e)) {
                String errorMessage = "유효하지 않은 이메일 형식입니다: " + e;
                logger.error(errorMessage);
                failedEmails.add(e);
                allSuccess = false;
                continue; // 이메일 형식이 유효하지 않으면 다음 이메일로 넘어감
            }

            try {
                // 이메일 검증 요청 생성
                VerifyEmailIdentityRequest request = new VerifyEmailIdentityRequest()
                        .withEmailAddress(e);

                // AWS SES 클라이언트를 사용하여 이메일 검증 요청
                VerifyEmailIdentityResult response = amazonSimpleEmailService.verifyEmailIdentity(request);

                // 이메일 검증 이메일 발송 완료 로그
                logger.info("Verification email sent to " + e);
            } catch (AmazonServiceException ase) {
                // AWS 서비스 예외 처리
                String errorMessage = "이메일 인증 요청을 보내는 데 실패했습니다: " + e + ". 오류 메시지: " + ase.getMessage();
                logger.error(errorMessage);
                failedEmails.add(e);
                allSuccess = false;
            } catch (AmazonClientException ace) {
                // AWS 클라이언트 예외 처리
                String errorMessage = "클라이언트 오류가 발생했습니다: " + e + ". 오류 메시지: " + ace.getMessage();
                logger.error(errorMessage);
                failedEmails.add(e);
                allSuccess = false;
            } catch (Exception ex) {
                // 일반 예외 처리
                String errorMessage = "예상치 못한 오류가 발생했습니다: " + e + ". 오류 메시지: " + ex.getMessage();
                logger.error(errorMessage);
                failedEmails.add(e);
                allSuccess = false;
            }
        }

        // SetEmailsResponse 객체 생성 및 반환
        SetEmailsResponse response = new SetEmailsResponse();
        if (allSuccess) {
            response.setMessage("모든 이메일 인증 요청을 성공적으로 보냈습니다.");
        } else {
            response.setMessage("일부 이메일 인증 요청에 실패했습니다.");
        }
        response.setEmail(failedEmails);
        return response;
    }
}

