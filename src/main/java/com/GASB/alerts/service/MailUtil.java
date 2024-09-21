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
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

        // 메일 제목 및 발신자 설정
        message.setSubject(title);
        message.setFrom(new InternetAddress("gasb@grummang.com"));

        // 수신자 설정
        String recipients = String.join(",", receivers);
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));

        // HTML 템플릿 파일 읽기
        ClassPathResource resource = new ClassPathResource("templates/index.html");
        String htmlTemplate = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);

        // 변수 설정
        String saasName = fileUpload.getOrgSaaS().getSaas().getSaasName();
        String uploadChannel = activities.getUploadChannel();
        String fileName = activities.getFileName();
        String uploadTimestamp = fileUpload.getTimestamp().toString(); // 필요한 형식으로 변환

        // 변수 대입
        htmlTemplate = htmlTemplate
                .replace("[[imageUrl]]", "https://staticfile-template.s3.ap-northeast-2.amazonaws.com/grum.png")
                .replace("[[title]]", "구름망 CASB")
                .replace("[[issueText]]", title)
                .replace("[[content]]", content)
                .replace("[[saasName]]", saasName)
                .replace("[[uploadChannel]]", uploadChannel)
                .replace("[[fileName]]", fileName)
                .replace("[[uploadTime]]", uploadTimestamp);

        // 메일 본문 생성
        MimeMultipart msg = new MimeMultipart("mixed");

        // 텍스트 파트 생성
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(content, "text/plain; charset=UTF-8");
        msg.addBodyPart(textPart);

        // HTML 파트 생성
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlTemplate, "text/html; charset=UTF-8");
        msg.addBodyPart(htmlPart);

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
