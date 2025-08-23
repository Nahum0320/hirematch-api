package com.hirematch.hirematch_api.service;

import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerificationService {

    private final EmailService emailService;
    private final UsuarioRepository usuarioRepository;
    private final ConcurrentHashMap<String, VerificationCode> verificationCodes = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public VerificationService(EmailService emailService, UsuarioRepository usuarioRepository) {
        this.emailService = emailService;
        this.usuarioRepository = usuarioRepository;
    }

    public String generarCodigoVerificacion(String email, String nombre) {
        // Generar código de 6 dígitos
        String codigo = String.format("%06d", random.nextInt(1000000));

        // Crear objeto de verificación
        VerificationCode verificationCode = new VerificationCode(
                codigo,
                email,
                LocalDateTime.now().plusMinutes(30) // Válido por 30 minutos
        );

        // Almacenar código (usar email como clave)
        verificationCodes.put(email, verificationCode);

        // Limpiar códigos expirados
        limpiarCodigosExpirados();

        // Enviar correo
        boolean enviado = emailService.enviarCorreoVerificacion(email, nombre, codigo);

        if (!enviado) {
            verificationCodes.remove(email);
            throw new ValidacionException("Error al enviar el correo de verificación");
        }

        return codigo;
    }

    public boolean verificarCodigo(String email, String codigo) {
        // Buscar usuario
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ValidacionException("Usuario no encontrado"));

        // Verificar si tiene código asignado
        if (usuario.getCodigoVerificacion() == null || usuario.getFechaExpiracionCodigo() == null) {
            return false;
        }

        // Verificar si el código ha expirado
        if (LocalDateTime.now().isAfter(usuario.getFechaExpiracionCodigo())) {
            usuario.setCodigoVerificacion(null); // limpiar código
            usuarioRepository.save(usuario);
            return false;
        }

        // Verificar si el código coincide
        boolean isValid = usuario.getCodigoVerificacion().equals(codigo);

        if (isValid) {
            usuario.setCodigoVerificacion(null); // eliminar código usado
            usuario.setEmailVerificado(true);     // marcar como verificado
            usuarioRepository.save(usuario);
        }

        return isValid;
    }


    public void reenviarCodigo(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ValidacionException("Usuario no encontrado"));

        if (usuario.getEmailVerificado()) {
            throw new ValidacionException("El usuario ya está verificado");
        }

        generarCodigoVerificacion(email, usuario.getNombre());
    }

    private void limpiarCodigosExpirados() {
        LocalDateTime now = LocalDateTime.now();
        verificationCodes.entrySet().removeIf(entry ->
                now.isAfter(entry.getValue().getExpiresAt())
        );
    }

    // Clase interna para representar un código de verificación
    private static class VerificationCode {
        private final String codigo;
        private final String email;
        private final LocalDateTime expiresAt;

        public VerificationCode(String codigo, String email, LocalDateTime expiresAt) {
            this.codigo = codigo;
            this.email = email;
            this.expiresAt = expiresAt;
        }

        public String getCodigo() { return codigo; }
        public String getEmail() { return email; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
    }
}