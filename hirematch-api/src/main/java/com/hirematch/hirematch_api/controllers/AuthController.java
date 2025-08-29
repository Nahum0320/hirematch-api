package com.hirematch.hirematch_api.controllers;

import com.hirematch.hirematch_api.DTO.*;
import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.entity.Sesion;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.SesionRepository;
import com.hirematch.hirematch_api.repository.UsuarioRepository;
import com.hirematch.hirematch_api.security.TokenService;
import com.hirematch.hirematch_api.service.PasswordService;
import com.hirematch.hirematch_api.service.SesionService;
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
    private final SesionService sesionService;

    public AuthController(UsuarioRepository usuarioRepository,
                          TokenService tokenService,
                          PasswordService passwordService,
                          VerificationService verificationService,
                          SesionService sesionService, SesionRepository sesionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
        this.passwordService = passwordService;
        this.verificationService = verificationService;
        this.sesionService = sesionService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ValidacionException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();

        String codigoVerificacion = " ";

        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(request.getPassword()); // se encripta en @PrePersist
        usuario.setEmailVerificado(false); // Email no verificado hasta confirmar código
        usuario.setCodigoVerificacion(codigoVerificacion);
        usuarioRepository.save(usuario);

        // Generar y enviar código de verificación
        try {
            codigoVerificacion = verificationService.generarCodigoVerificacion(request.getEmail(), request.getNombre());
        } catch (ValidacionException e) {
            // Si falla el envío del correo, eliminar el usuario creado
            usuarioRepository.delete(usuario);
            throw e;
        }

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
         
        Sesion sesion = sesionService.crearSesion(usuario);
        String token = tokenService.generarToken(sesion);
        LoginResponse response = new LoginResponse(
                "Se ha logrado iniciar sesión",
                token
        );
        usuario.setUltimoAcceso(java.time.LocalDateTime.now());
        usuarioRepository.save(usuario);
        return ResponseEntity.ok(response);
    }

  @PostMapping("/logout")
public ResponseEntity<LogoutResponse> logout(@RequestHeader("Authorization") String authHeader) {
    try {
        // Validar formato del header
        if (!authHeader.startsWith("Bearer ")) {
            LogoutResponse response = new LogoutResponse();
            response.setMessage("Formato de token inválido");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Extraer token
        String token = authHeader.substring(7); 
        
        // Validar que el token no esté vacío
        if (token.trim().isEmpty()) {
            LogoutResponse response = new LogoutResponse();
            response.setMessage("Token vacío");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Obtener subject del token
        String subject = tokenService.getSubject(token);
        
        if (subject == null || subject.trim().isEmpty()) {
            LogoutResponse response = new LogoutResponse();
            response.setMessage("Token inválido");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Convertir a Long y cerrar sesión
        Long numeroSesion = Long.parseLong(subject);
        if(sesionService.obtenerSesion(numeroSesion).isActiva())
        {
            LogoutResponse response = new LogoutResponse();
            sesionService.cerrarSesion(numeroSesion);
            response.setMessage("Sesión cerrada exitosamente");
            return ResponseEntity.ok(response);
        }
        else {
            LogoutResponse response = new LogoutResponse();
            response.setMessage("La sesión ya estaba cerrada o expirada");
            return ResponseEntity.ok(response);
        }
        
        
    } catch (NumberFormatException e) {
        LogoutResponse response = new LogoutResponse();
        response.setMessage("Sesión no válida");
        return ResponseEntity.badRequest().body(response);
    } catch (Exception e) {
        // Capturar cualquier otra excepción (token expirado, malformado, etc.)
        LogoutResponse response = new LogoutResponse();
        response.setMessage("Error al procesar el token");
        return ResponseEntity.badRequest().body(response);
    }
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