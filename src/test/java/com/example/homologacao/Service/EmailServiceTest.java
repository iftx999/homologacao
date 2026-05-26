package com.example.homologacao.Service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Test
    void enviaEmailComRemetenteEDestinatariosConfigurados() {
        EmailService emailService = new EmailService(mailSender, "alertas@empresa.com");

        emailService.enviarEmail(
                "ti@empresa.com, produto@empresa.com",
                "Implantacao nao homologada",
                "Existem pendencias."
        );

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();
        assertThat(message.getFrom()).isEqualTo("alertas@empresa.com");
        assertThat(message.getTo()).containsExactly("ti@empresa.com", "produto@empresa.com");
        assertThat(message.getSubject()).isEqualTo("Implantacao nao homologada");
        assertThat(message.getText()).isEqualTo("Existem pendencias.");
    }

    @Test
    void rejeitaEnvioSemDestinatario() {
        EmailService emailService = new EmailService(mailSender, "");

        assertThatThrownBy(() -> emailService.enviarEmail("", "Assunto", "Corpo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("destinat");

        verifyNoInteractions(mailSender);
    }
}
