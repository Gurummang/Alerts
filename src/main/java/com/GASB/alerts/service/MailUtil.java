package com.GASB.alerts.service;

import com.GASB.alerts.model.entity.Activities;
import com.GASB.alerts.model.entity.FileUpload;
import com.GASB.alerts.repository.ActivitiesRepo;
import jakarta.mail.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import com.amazonaws.services.simpleemail.model.RawMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Properties;


@Slf4j
@Component
@RequiredArgsConstructor
public class MailUtil {

    private final ActivitiesRepo activitiesRepo;

    public SendRawEmailRequest getSendRawEmailRequest(String title, String content, List<String> receivers, FileUpload fileUpload) throws MessagingException, IOException {
        Activities activities = activitiesRepo.findAllBySaasFileIdAndTimeStamp(fileUpload.getSaasFileId(), fileUpload.getTimestamp());

        log.info("메일 전송 중");
        // 유효성 검사
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("메일 제목은 필수입니다.");
        }
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("메일 내용은 필수입니다.");
        }
        if (receivers == null || receivers.isEmpty()) {
            throw new IllegalArgumentException("수신자 목록은 비어 있을 수 없습니다.");
        }

        // 세션 생성
        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage message = new MimeMessage(session);

        // 메일 제목 설정
        message.setSubject(title);

        // 발신자 설정
        message.setFrom(new InternetAddress("gasb@grummang.com"));

        // 수신자 설정
        String recipients = String.join(",", receivers);
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));

        // 메일 본문 생성
        MimeMultipart msg = new MimeMultipart("mixed");

        // 텍스트 파트 생성
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(content, "text/plain; charset=UTF-8");
        msg.addBodyPart(textPart);

        // 파일 정보 추가 (커스텀 내용)
        StringBuilder fileInfo = new StringBuilder("< 파일 정보 >\n");
        fileInfo.append("SaaS: ").append(fileUpload.getOrgSaaS().getSaas().getSaasName()).append("\n");
        fileInfo.append("업로드 채널: ").append(activities.getUploadChannel()).append("\n");
        fileInfo.append("파일 이름: ").append(activities.getFileName()).append("\n");
        fileInfo.append("파일 업로드 시각: ").append(fileUpload.getTimestamp()).append("\n");


        // 파일 정보 텍스트 파트로 추가
        MimeBodyPart fileInfoPart = new MimeBodyPart();
        fileInfoPart.setContent(fileInfo.toString(), "text/plain; charset=UTF-8");
        msg.addBodyPart(fileInfoPart);


        // 메일 콘텐츠 설정
        message.setContent(msg);



        // 메일을 RawMessage로 변환
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            message.writeTo(outputStream);
            RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));
            return new SendRawEmailRequest(rawMessage);
        } catch (MessagingException | IOException e) {
            log.info("이메일 작성 중 오류 발생: " + e.getMessage());
            throw e;
        }
    }
}

