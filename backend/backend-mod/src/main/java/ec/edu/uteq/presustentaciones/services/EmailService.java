package ec.edu.uteq.presustentaciones.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.enabled:true}")
    private boolean enabled;

    @Async
    public void enviarNotificacion(String destinatario, String mensaje) {
        if (!enabled) {
            log.info("[EMAIL DESHABILITADO] Para: {} | Mensaje: {}", destinatario, mensaje);
            return;
        }
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(from);
            mail.setTo(destinatario);
            mail.setSubject("Sistema de Pre-Sustentaciones - Nueva Notificación");
            mail.setText(
                    "Estimado/a usuario/a,\n\n" +
                            "Tienes una nueva notificación en el Sistema de Pre-Sustentaciones:\n\n" +
                            "  " + mensaje + "\n\n" +
                            "Por favor ingresa al sistema para ver más detalles.\n\n" +
                            "Este es un mensaje automático, por favor no respondas a este correo.\n" +
                            "Sistema de Pre-Sustentaciones - UTEQ"
            );
            mailSender.send(mail);
            log.info("Email enviado a: {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar email a {}: {}", destinatario, e.getMessage());
        }
    }
}