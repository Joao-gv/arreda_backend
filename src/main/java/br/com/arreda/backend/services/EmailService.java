package br.com.arreda.backend.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/** Serviço compartilhado para envio assíncrono de e-mails HTML. */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String remetente;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${arreda.email.from}") String remetente
    ) {
        this.mailSender = mailSender;
        this.remetente = remetente;
    }

    /**
     * Envia uma mensagem HTML sem bloquear a requisição que iniciou a operação.
     * Falhas de transporte ou montagem são registradas e não propagadas ao chamador.
     */
    @Async
    public void enviar(String destinatario, String assunto, String corpoHtml) {
        try {
            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mensagem,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );
            helper.setFrom(remetente);
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(corpoHtml, true);

            mailSender.send(mensagem);
        } catch (MessagingException | MailException exception) {
            // Não registrar o destinatário para evitar exposição de dado pessoal nos logs.
            log.error("Falha ao enviar e-mail com assunto '{}'.", assunto, exception);
        }
    }
}
