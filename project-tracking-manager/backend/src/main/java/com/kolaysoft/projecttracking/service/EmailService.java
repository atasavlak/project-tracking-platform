package com.kolaysoft.projecttracking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value(
            "${app.frontend.activation-url:http://localhost:3000/activate}"
    )
    private String activationUrl;

    @Value(
            "${app.frontend.password-reset-url:http://localhost:3000/reset-password}"
    )
    private String passwordResetUrl;

    @Value("${app.mail.from:no-reply@projecttracking.local}")
    private String fromAddress;

    public void sendActivationEmail(
            String recipient,
            String fullName,
            String rawToken,
            LocalDateTime expiresAt
    ) {
        String link =
                activationUrl
                        + "?token="
                        + rawToken;

        String content =
                "Merhaba "
                        + fullName
                        + ",\n\n"
                        + "Project Tracking hesabınız oluşturuldu.\n\n"
                        + "İlk şifrenizi belirlemek için aşağıdaki bağlantıyı kullanabilirsiniz:\n"
                        + link
                        + "\n\n"
                        + "Swagger üzerinden test etmek için aktivasyon tokenınız:\n"
                        + rawToken
                        + "\n\n"
                        + "Token geçerlilik süresi: "
                        + expiresAt
                        + "\n\n"
                        + "Bu işlemi siz beklemiyorsanız sistem yöneticisiyle iletişime geçiniz.";

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(fromAddress);

        message.setTo(recipient);

        message.setSubject(
                "Project Tracking Hesap Aktivasyonu"
        );

        message.setText(content);

        javaMailSender.send(message);
    }

    public void sendPasswordResetEmail(
            String recipient,
            String fullName,
            String rawToken,
            LocalDateTime expiresAt
    ) {
        String link =
                passwordResetUrl
                        + "?token="
                        + rawToken;

        String content =
                "Merhaba "
                        + fullName
                        + ",\n\n"
                        + "Project Tracking hesabınız için şifre sıfırlama talebi alındı.\n\n"
                        + "Yeni şifrenizi belirlemek için aşağıdaki bağlantıyı kullanabilirsiniz:\n"
                        + link
                        + "\n\n"
                        + "Swagger üzerinden test etmek için şifre sıfırlama tokenınız:\n"
                        + rawToken
                        + "\n\n"
                        + "Token geçerlilik süresi: "
                        + expiresAt
                        + "\n\n"
                        + "Bu işlemi siz talep etmediyseniz bu e-postayı dikkate almayınız.";

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(fromAddress);

        message.setTo(recipient);

        message.setSubject(
                "Project Tracking Şifre Sıfırlama"
        );

        message.setText(content);

        javaMailSender.send(message);
    }
}