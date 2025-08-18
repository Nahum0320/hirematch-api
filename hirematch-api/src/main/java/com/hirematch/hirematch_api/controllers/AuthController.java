package com.hirematch.hirematch_api.controllers;

import com.hirematch.hirematch_api.DTO.LoginRequest;
import com.hirematch.hirematch_api.DTO.LoginResponse;
import com.hirematch.hirematch_api.DTO.RegisterRequest;
import com.hirematch.hirematch_api.DTO.RegisterResponse;
import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.UsuarioRepository;
import com.hirematch.hirematch_api.security.TokenService;
import com.hirematch.hirematch_api.service.PasswordService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final PasswordService passwordService;

    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;


    public AuthController(UsuarioRepository usuarioRepository,
                          TokenService tokenService, PasswordService passwordService) {
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
        this.passwordService = passwordService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ValidacionException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(request.getPassword()); // se encripta en @PrePersist
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(new RegisterResponse(usuario.getUsuarioId(), usuario.getNombre(), usuario.getEmail(), "Usuario registrado correctamente"));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ValidacionException("Credenciales inválidas"));

        if (!passwordService.verificar(request.getPassword(), usuario.getPassword())) {
            throw new ValidacionException("Credenciales inválidas");
        }

        String token = tokenService.generarToken(usuario);

        LoginResponse response = new LoginResponse(
                "Se ha logrado iniciar sesión",
                token
        );

        return ResponseEntity.ok(response);
    }
    
}

