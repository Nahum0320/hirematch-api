package com.hirematch.hirematch_api.service;

import com.hirematch.hirematch_api.DTO.ChatResponse;
import com.hirematch.hirematch_api.DTO.MensajeResponse;
import com.hirematch.hirematch_api.DTO.MensajesPorChatResponse;
import com.hirematch.hirematch_api.ValidacionException;
import com.hirematch.hirematch_api.entity.*;
import com.hirematch.hirematch_api.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final MensajeRepository mensajeRepository;
    private final PerfilRepository perfilRepository;
    private final PostulantePorOfertaRepository postulacionRepository;
    private final OfertaLaboralRepository ofertaRepository;
    private final EmpresaRepository empresaRepository;

    public ChatService(ChatRepository chatRepository, MensajeRepository mensajeRepository,
                       PerfilRepository perfilRepository, PostulantePorOfertaRepository postulacionRepository,
                       OfertaLaboralRepository ofertaRepository, EmpresaRepository empresaRepository) {
        this.chatRepository = chatRepository;
        this.mensajeRepository = mensajeRepository;
        this.perfilRepository = perfilRepository;
        this.postulacionRepository = postulacionRepository;
        this.ofertaRepository = ofertaRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional
    public MensajeResponse enviarMensaje(Usuario usuario, Long ofertaId, String contenido) {
        // Validar usuario y perfil
        Perfil perfil = perfilRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ValidacionException("Perfil no encontrado"));

        // Validar tipo de perfil
        boolean isPostulante = "POSTULANTE".equalsIgnoreCase(perfil.getTipoPerfil());
        boolean isEmpresa = "EMPRESA".equalsIgnoreCase(perfil.getTipoPerfil());
        if (!isPostulante && !isEmpresa) {
            throw new ValidacionException("Solo postulantes o empresas pueden enviar mensajes");
        }

        // Validar oferta
        OfertaLaboral oferta = ofertaRepository.findById(ofertaId)
                .orElseThrow(() -> new ValidacionException("Oferta no encontrada"));

        // Determinar postulante y empresa según el tipo de perfil
        Perfil postulante;
        Empresa empresa;
        if (isPostulante) {
            postulante = perfil;
            empresa = oferta.getEmpresa();
        } else {
            empresa = empresaRepository.findByUsuario(usuario)
                    .orElseThrow(() -> new ValidacionException("Empresa no encontrada"));
            postulante = postulacionRepository.findByOfertaAndEstado(oferta, EstadoPostulacion.MATCHED)
                    .stream()
                    .filter(p -> p.isMatched())
                    .map(PostulantePorOferta::getPostulante)
                    .findFirst()
                    .orElseThrow(() -> new ValidacionException("No existe un match para esta oferta"));
        }

        // Verificar match
        PostulantePorOferta postulacion = postulacionRepository.findByPostulanteAndOferta(postulante, oferta)
                .orElseThrow(() -> new ValidacionException("No existe postulación para esta oferta"));
        if (!postulacion.isMatched()) {
            throw new ValidacionException("Se requiere un match para enviar mensajes");
        }

        // Buscar o crear chat
        Chat chat = chatRepository.findByPostulanteAndEmpresaAndOferta(postulante, empresa, oferta)
                .orElseGet(() -> {
                    Chat newChat = new Chat();
                    newChat.setPostulante(postulante);
                    newChat.setEmpresa(empresa);
                    newChat.setOferta(oferta);
                    return chatRepository.save(newChat);
                });

        // Validar contenido
        if (contenido == null || contenido.trim().isEmpty()) {
            throw new ValidacionException("El contenido del mensaje no puede estar vacío");
        }
        if (contenido.length() > 1000) {
            throw new ValidacionException("El contenido del mensaje no puede exceder 1000 caracteres");
        }

        // Crear mensaje
        Mensaje mensaje = new Mensaje();
        mensaje.setChat(chat);
        mensaje.setRemitente(perfil);
        mensaje.setContenido(contenido);
        mensaje.setFechaEnvio(LocalDateTime.now());
        mensajeRepository.save(mensaje);

        // Mapear a DTO
        return mapToMensajeResponse(mensaje);
    }

    public List<ChatResponse> obtenerChats(Usuario usuario) {
        Perfil perfil = perfilRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ValidacionException("Perfil no encontrado"));

        boolean isPostulante = "POSTULANTE".equalsIgnoreCase(perfil.getTipoPerfil());
        List<Chat> chats;

        if (isPostulante) {
            // Para postulantes: chats donde es postulante
            chats = chatRepository.findByPostulante(perfil);
        } else {
            // Para empresas: chats donde es empresa
            Empresa empresa = empresaRepository.findByUsuario(usuario)
                    .orElseThrow(() -> new ValidacionException("Empresa no encontrada"));
            chats = chatRepository.findByEmpresa(empresa);
        }

        return chats.stream()
                .map(this::mapToChatResponse)
                .collect(Collectors.toList());
    }

    public MensajesPorChatResponse obtenerMensajesPorChat(Long chatId, Usuario usuario) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ValidacionException("Chat no encontrado"));

        // Verificar autorización: el usuario debe ser postulante o empresa del chat
        Perfil perfil = perfilRepository.findByUsuario(usuario)
                .orElseThrow(() -> new ValidacionException("Perfil no encontrado"));
        boolean isAuthorized = chat.getPostulante().equals(perfil) ||
                empresaRepository.findByUsuario(usuario).map(e -> e.equals(chat.getEmpresa())).orElse(false);
        if (!isAuthorized) {
            throw new ValidacionException("No autorizado para ver este chat");
        }

        List<Mensaje> mensajes = mensajeRepository.findByChatOrderByFechaEnvioDesc(chat);
        List<MensajeResponse> mensajeResponses = mensajes.stream()
                .map(this::mapToMensajeResponse)
                .collect(Collectors.toList());

        MensajesPorChatResponse response = new MensajesPorChatResponse();
        response.setChatId(chatId);
        response.setMensajes(mensajeResponses);
        return response;
    }

    private ChatResponse mapToChatResponse(Chat chat) {
        ChatResponse response = new ChatResponse();
        response.setId(chat.getId());
        response.setOfertaId(chat.getOferta().getId());
        response.setTituloOferta(chat.getOferta().getTitulo());

        // Determinar nombre de la contraparte
        if (chat.getPostulante() != null && chat.getPostulante().getUsuario() != null) {
            response.setNombreContraparte(chat.getPostulante().getUsuario().getNombre() + " " +
                    chat.getPostulante().getUsuario().getApellido());
        } else if (chat.getEmpresa() != null) {
            response.setNombreContraparte(chat.getEmpresa().getNombreEmpresa());
        }

        // Último mensaje (simplificado: obtener el más reciente)
        Optional<Mensaje> ultimoMensaje = mensajeRepository.findTopByChatOrderByFechaEnvioDesc(chat);
        ultimoMensaje.ifPresent(mensaje -> {
            response.setUltimoMensaje(mensaje.getContenido());
            response.setUltimaActividad(mensaje.getFechaEnvio());
        });

        // No leídos: contar mensajes no leídos (simplificado, asumiendo lógica de lectura)
        response.setNoLeidos(0L); // Implementar lógica real si es necesario

        return response;
    }

    private MensajeResponse mapToMensajeResponse(Mensaje mensaje) {
        MensajeResponse response = new MensajeResponse();
        response.setId(mensaje.getId());
        response.setChatId(mensaje.getChat().getId());
        response.setRemitenteId(mensaje.getRemitente().getPerfilId());
        response.setContenido(mensaje.getContenido());
        response.setFechaEnvio(mensaje.getFechaEnvio());
        return response;
    }

    private List<Chat> findByOferta_Id(Long ofertaId) {
        return chatRepository.findByOferta_Id(ofertaId);
    }
}