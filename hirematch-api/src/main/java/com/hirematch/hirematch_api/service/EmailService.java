package com.hirematch.hirematch_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.logging.Logger;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private static final Logger logger = Logger.getLogger(EmailService.class.getName());

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean enviarCorreoVerificacion(String emailDestinatario, String nombreDestinatario, String codigo) {
        try {
            logger.info("Enviando correo de verificación a: " + emailDestinatario + " con código: " + codigo);

            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(fromEmail, "HireMatch");
            helper.setTo(emailDestinatario);
            helper.setSubject("Código de Verificación - HireMatch");

            String htmlContent = String.format("""
                <html>
                <body style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;'>
                    <div style='max-width: 600px; margin: auto; background-color: #ffffff; padding: 20px; border-radius: 10px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);'>
                        <h2 style='color: #4CAF50;'>¡Hola %s!</h2>
                        <p style='font-size: 16px;'>Gracias por registrarte en <strong>HireMatch</strong>.</p>
                        <p style='font-size: 16px;'>Tu código de verificación es:</p>
                        <div style='font-size: 24px; font-weight: bold; color: #333; background-color: #e7f3fe; padding: 10px; border-radius: 8px; text-align: center;'>
                            %s
                        </div>
                        <p style='font-size: 14px; color: #555;'>Este código es válido por 30 minutos.</p>
                        <hr style='margin: 30px 0;'>
                        <p style='font-size: 12px; color: #999;'>Si no solicitaste este código, puedes ignorar este mensaje.</p>
                        <p style='font-size: 14px;'>Atentamente,<br>El equipo de <strong>HireMatch</strong></p>
                    </div>
                </body>
                </html>
                """, nombreDestinatario, codigo);

            helper.setText(htmlContent, true);

            mailSender.send(mensaje);
            logger.info("Correo enviado con éxito a: " + emailDestinatario);
            return true;

        } catch (MessagingException e) {
            logger.severe("Error al enviar correo a " + emailDestinatario + ": " + e.getMessage());
            return false;
        } catch (Exception e) {
            logger.severe("Error general al enviar correo a " + emailDestinatario + ": " + e.getMessage());
            return false;
        }
    }

    public boolean enviarCorreoRecuperacion(String emailDestinatario, String nombreDestinatario, String codigo) {
        try {
            logger.info("Enviando correo de recuperación a: " + emailDestinatario + " con código: " + codigo);

            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(fromEmail, "HireMatch");
            helper.setTo(emailDestinatario);
            helper.setSubject("Código de Recuperación de Contraseña - HireMatch");

            String htmlContent = String.format("""
                <html>
                <body style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;'>
                    <div style='max-width: 600px; margin: auto; background-color: #ffffff; padding: 20px; border-radius: 10px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);'>
                        <h2 style='color: #4CAF50;'>¡Hola %s!</h2>
                        <p style='font-size: 16px;'>Hemos recibido una solicitud para cambiar tu contraseña en <strong>HireMatch</strong>.</p>
                        <p style='font-size: 16px;'>Utiliza el siguiente código para confirmar el cambio de contraseña:</p>
                        <div style='font-size: 24px; font-weight: bold; color: #333; background-color: #e7f3fe; padding: 10px; border-radius: 8px; text-align: center;'>
                            %s
                        </div>
                        <p style='font-size: 14px; color: #555;'>Este código es válido por 30 minutos.</p>
                        <hr style='margin: 30px 0;'>
                        <p style='font-size: 12px; color: #999;'>Si no solicitaste cambiar tu contraseña, por favor ignora este mensaje o contacta con nuestro soporte.</p>
                        <p style='font-size: 14px;'>Atentamente,<br>El equipo de <strong>HireMatch</strong></p>
                    </div>
                </body>
                </html>
                """, nombreDestinatario, codigo);

            helper.setText(htmlContent, true);

            mailSender.send(mensaje);
            logger.info("Correo de recuperación enviado con éxito a: " + emailDestinatario);
            return true;

        } catch (MessagingException e) {
            logger.severe("Error al enviar correo de recuperación a " + emailDestinatario + ": " + e.getMessage());
            return false;
        } catch (Exception e) {
            logger.severe("Error general al enviar correo de recuperación a " + emailDestinatario + ": " + e.getMessage());
            return false;
        }
    }
}
