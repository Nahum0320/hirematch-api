package com.hirematch.hirematch_api.controllers;

import com.hirematch.hirematch_api.DTO.ChatResponse;
import com.hirematch.hirematch_api.DTO.MensajeRequest;
import com.hirematch.hirematch_api.DTO.MensajesPorChatResponse;
import com.hirematch.hirematch_api.DTO.MensajeResponse;
import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.entity.Sesion;
import com.hirematch.hirematch_api.entity.Usuario;
import com.hirematch.hirematch_api.repository.SesionRepository;
import com.hirematch.hirematch_api.security.TokenService;
import com.hirematch.hirematch_api.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final TokenService tokenService;
    private final SesionRepository sesionRepository;

    public ChatController(ChatService chatService, TokenService tokenService, SesionRepository sesionRepository) {
        this.chatService = chatService;
        this.tokenService = tokenService;
        this.sesionRepository = sesionRepository;
    }

    @PostMapping("/mensaje")
    public ResponseEntity<MensajeResponse> enviarMensaje(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody MensajeRequest request) {
        Usuario usuario = obtenerUsuarioAutenticado(authHeader);
        MensajeResponse response = chatService.enviarMensaje(usuario, request.getOfertaId(), request.getContenido());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/chats")
    public ResponseEntity<List<ChatResponse>> obtenerChats(@RequestHeader("Authorization") String authHeader) {
        Usuario usuario = obtenerUsuarioAutenticado(authHeader);
        List<ChatResponse> chats = chatService.obtenerChats(usuario);
        return ResponseEntity.ok(chats);
    }

    @GetMapping("/mensajes/{chatId}")
    public ResponseEntity<MensajesPorChatResponse> obtenerMensajes(@PathVariable Long chatId,
                                                                   @RequestHeader("Authorization") String authHeader) {
        Usuario usuario = obtenerUsuarioAutenticado(authHeader);
        MensajesPorChatResponse response = chatService.obtenerMensajesPorChat(chatId, usuario);
        return ResponseEntity.ok(response);
    }

    private Usuario obtenerUsuarioAutenticado(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ValidacionException("Token de autorización requerido");
        }

        String token = authHeader.substring(7);
        if (token.trim().isEmpty()) {
            throw new ValidacionException("Token vacío");
        }

        try {
            String subject = tokenService.getSubject(token);
            if (subject == null || subject.trim().isEmpty()) {
                throw new ValidacionException("Token inválido");
            }

            Long numeroSesion = Long.parseLong(subject);
            Sesion sesion = sesionRepository.findById(numeroSesion)
                    .orElseThrow(() -> new ValidacionException("Sesión no encontrada"));

            if (!sesion.isActiva() || sesion.hasExpired()) {
                throw new ValidacionException("Sesión inactiva o expirada");
            }

            return sesion.getUsuario();
        } catch (NumberFormatException e) {
            throw new ValidacionException("Token de sesión inválido");
        } catch (Exception e) {
            throw new ValidacionException("Error al procesar el token: " + e.getMessage());
        }
    }
}