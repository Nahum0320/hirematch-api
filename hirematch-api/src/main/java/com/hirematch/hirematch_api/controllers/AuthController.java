package com.hirematch.hirematch_api.controllers;

import com.hirematch.hirematch_api.DTO.*;
import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.UsuarioRepository;
import com.hirematch.hirematch_api.security.TokenService;
import com.hirematch.hirematch_api.service.PasswordService;
import com.hirematch.hirematch_api.service.VerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final PasswordService passwordService;
    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;
    private final VerificationService verificationService;

    public AuthController(UsuarioRepository usuarioRepository,
                          TokenService tokenService,
                          PasswordService passwordService,
                          VerificationService verificationService) {
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
        this.passwordService = passwordService;
        this.verificationService = verificationService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ValidacionException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();

        String codigoVerificacion = " ";

        // Generar y enviar código de verificación
        try {
            codigoVerificacion = verificationService.generarCodigoVerificacion(request.getEmail(), request.getNombre());
        } catch (ValidacionException e) {
            // Si falla el envío del correo, eliminar el usuario creado
            usuarioRepository.delete(usuario);
            throw e;
        }

        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(request.getPassword()); // se encripta en @PrePersist
        usuario.setEmailVerificado(false); // Email no verificado hasta confirmar código
        usuario.setCodigoVerificacion(codigoVerificacion);
        usuarioRepository.save(usuario);


        return ResponseEntity.ok(new RegisterResponse(
                usuario.getUsuarioId(),
                usuario.getNombre(),
                usuario.getEmail(),
                "Usuario registrado. Se ha enviado un código de verificación a tu correo electrónico."
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ValidacionException("Credenciales inválidas"));

        if (!passwordService.verificar(request.getPassword(), usuario.getPassword())) {
            throw new ValidacionException("Credenciales inválidas");
        }

        // Verificar que el email esté verificado
        if (!usuario.getEmailVerificado()) {
            throw new ValidacionException("Debes verificar tu correo electrónico antes de iniciar sesión");
        }

        String token = tokenService.generarToken(usuario);

        LoginResponse response = new LoginResponse(
                "Se ha logrado iniciar sesión",
                token
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<VerificacionResponse> verificarCorreo(@Valid @RequestBody VerificarCodigoRequest request) {
        boolean verificado = verificationService.verificarCodigo(request.getEmail(), request.getCodigo());

        if (verificado) {
            return ResponseEntity.ok(new VerificacionResponse(
                    "Correo verificado correctamente. Ya puedes iniciar sesión.",
                    true
            ));
        } else {
            return ResponseEntity.badRequest().body(new VerificacionResponse(
                    "Código inválido o expirado",
                    false
            ));
        }
    }

    @PostMapping("/resend-code")
    public ResponseEntity<VerificacionResponse> reenviarCodigo(@Valid @RequestBody ReenviarCodigoRequest request) {
        try {
            verificationService.reenviarCodigo(request.getEmail());
            return ResponseEntity.ok(new VerificacionResponse(
                    "Código de verificación reenviado correctamente",
                    false
            ));
        } catch (ValidacionException e) {
            return ResponseEntity.badRequest().body(new VerificacionResponse(
                    e.getMessage(),
                    false
            ));
        }
    }
}