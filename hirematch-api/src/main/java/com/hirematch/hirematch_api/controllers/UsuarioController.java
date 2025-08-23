package com.hirematch.hirematch_api.controllers;

import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @PatchMapping("/activo")
    public ResponseEntity<String> updateUserActivo(@AuthenticationPrincipal Usuario usuario) {
        if (usuario == null) {
            throw new ValidacionException("Usuario no autenticado");
        }

        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok("Usuario activado correctamente");
    }
}