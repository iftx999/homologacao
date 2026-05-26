package com.example.homologacao.Service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String remetente;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.email.remetente:${spring.mail.username:}}") String remetente) {
        this.mailSender = mailSender;
        this.remetente = remetente;
    }

    public void enviarEmail(String para, String assunto, String corpo) {
        List<String> destinatarios = Arrays.stream(StringUtils.commaDelimitedListToStringArray(para))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();

        if (destinatarios.isEmpty()) {
            throw new IllegalArgumentException("Ao menos um destinatário de email deve ser informado");
        }

        if (!StringUtils.hasText(assunto)) {
            throw new IllegalArgumentException("O assunto do email deve ser informado");
        }

        if (!StringUtils.hasText(corpo)) {
            throw new IllegalArgumentException("O corpo do email deve ser informado");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(destinatarios.toArray(String[]::new));
        message.setSubject(assunto);
        message.setText(corpo);

        if (StringUtils.hasText(remetente)) {
            message.setFrom(remetente);
        }

        mailSender.send(message);
    }
}
