package com.hirematch.hirematch_api.service;

import com.hirematch.hirematch_api.DTO.*;
import com.hirematch.hirematch_api.entity.*;
import com.hirematch.hirematch_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BadgeService {

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private UsuarioBadgeRepository usuarioBadgeRepository;

    @Autowired
    private EstadisticaUsuarioRepository estadisticaRepository;

    @Transactional
    public BadgeObtenidoResponse verificarYOtorgarBadges(Perfil perfil) {
        List<Badge> nuevosBadges = new ArrayList<>();
        EstadisticaUsuario estadisticas = obtenerOCrearEstadisticas(perfil);

        // Verificar badges de primer match
        if (estadisticas.getTotalMatches() == 1 && !tieneBadge(perfil, "PRIMER_MATCH")) {
            Badge badge = badgeRepository.findByNombreAndActivoTrue("PRIMER_MATCH")
                    .orElseThrow(() -> new RuntimeException("Badge PRIMER_MATCH no encontrado"));
            otorgarBadge(perfil, badge);
            nuevosBadges.add(badge);
        }

        // Verificar badges de cantidad de matches
        verificarBadgesPorCantidad(perfil, Badge.TipoBadge.MATCHES_CANTIDAD, 
                estadisticas.getTotalMatches(), nuevosBadges);

        // Verificar badges de likes
        verificarBadgesPorCantidad(perfil, Badge.TipoBadge.LIKES_CANTIDAD, 
                estadisticas.getTotalLikesDados(), nuevosBadges);

        // Verificar badges de superlikes
        verificarBadgesPorCantidad(perfil, Badge.TipoBadge.SUPERLIKES_CANTIDAD, 
                estadisticas.getTotalSuperlikesDados(), nuevosBadges);

        // Verificar badge de perfil completo
        if (estadisticas.getPerfilCompletado() && !tieneBadge(perfil, "PERFIL_COMPLETO")) {
            Badge badge = badgeRepository.findByNombreAndActivoTrue("PERFIL_COMPLETO")
                    .orElseThrow(() -> new RuntimeException("Badge PERFIL_COMPLETO no encontrado"));
            otorgarBadge(perfil, badge);
            nuevosBadges.add(badge);
        }

        return crearRespuestaBadgeObtenido(nuevosBadges);
    }

    private void verificarBadgesPorCantidad(Perfil perfil, Badge.TipoBadge tipo, 
                                           Integer cantidad, List<Badge> nuevosBadges) {
        List<Badge> badgesDisponibles = badgeRepository.findBadgesDisponiblesByTipoAndValor(tipo, cantidad);
        
        for (Badge badge : badgesDisponibles) {
            if (!tieneBadge(perfil, badge.getNombre()) && 
                cantidad >= badge.getCondicionRequerida()) {
                otorgarBadge(perfil, badge);
                nuevosBadges.add(badge);
            }
        }
    }

    private boolean tieneBadge(Perfil perfil, String nombreBadge) {
        Badge badge = badgeRepository.findByNombreAndActivoTrue(nombreBadge).orElse(null);
        if (badge == null) return false;
        return usuarioBadgeRepository.existsByPerfilAndBadge(perfil, badge);
    }

    @Transactional
    private void otorgarBadge(Perfil perfil, Badge badge) {
        if (!usuarioBadgeRepository.existsByPerfilAndBadge(perfil, badge)) {
            UsuarioBadge usuarioBadge = new UsuarioBadge();
            usuarioBadge.setPerfil(perfil);
            usuarioBadge.setBadge(badge);
            usuarioBadge.setFechaObtenido(LocalDateTime.now());
            usuarioBadge.setNotificado(false);
            usuarioBadgeRepository.save(usuarioBadge);
        }
    }

    public List<UsuarioBadgeResponse> obtenerBadgesUsuario(Perfil perfil) {
        List<UsuarioBadge> usuarioBadges = usuarioBadgeRepository.findByPerfilWithBadgeDetails(perfil);
        return usuarioBadges.stream()
                .map(this::convertirAUsuarioBadgeResponse)
                .collect(Collectors.toList());
    }

    public List<BadgeResponse> obtenerTodosBadges() {
        List<Badge> badges = badgeRepository.findByActivoTrueOrderByFechaCreacionAsc();
        return badges.stream()
                .map(this::convertirABadgeResponse)
                .collect(Collectors.toList());
    }

    public List<ProgresoResponse> obtenerProgresoBadges(Perfil perfil) {
        EstadisticaUsuario estadisticas = obtenerOCrearEstadisticas(perfil);
        List<ProgresoResponse> progresos = new ArrayList<>();

        // Progreso de matches
        agregarProgresoTipo(progresos, Badge.TipoBadge.MATCHES_CANTIDAD, 
                estadisticas.getTotalMatches(), "Matches", "favorite");

        // Progreso de likes
        agregarProgresoTipo(progresos, Badge.TipoBadge.LIKES_CANTIDAD, 
                estadisticas.getTotalLikesDados(), "Likes dados", "thumb_up");

        // Progreso de superlikes
        agregarProgresoTipo(progresos, Badge.TipoBadge.SUPERLIKES_CANTIDAD, 
                estadisticas.getTotalSuperlikesDados(), "SuperLikes dados", "star");

        return progresos;
    }

    private void agregarProgresoTipo(List<ProgresoResponse> progresos, Badge.TipoBadge tipo, 
                                   Integer valorActual, String descripcionBase, String icono) {
        List<Badge> badges = badgeRepository.findByTipoAndActivoTrue(tipo);
        
        for (Badge badge : badges) {
            if (valorActual < badge.getCondicionRequerida()) {
                ProgresoResponse progreso = new ProgresoResponse();
                progreso.setTipoBadge(badge.getTipo().name());
                progreso.setNombreBadge(badge.getNombre());
                progreso.setDescripcion(badge.getDescripcion());
                progreso.setProgresoActual(valorActual);
                progreso.setProgresoRequerido(badge.getCondicionRequerida());
                progreso.setPorcentajeCompletado(
                    (double) valorActual / badge.getCondicionRequerida() * 100);
                progreso.setIcono(badge.getIcono());
                progreso.setColor(badge.getColor());
                
                progresos.add(progreso);
                break; // Solo mostrar el próximo badge a obtener
            }
        }
    }

    private EstadisticaUsuario obtenerOCrearEstadisticas(Perfil perfil) {
        return estadisticaRepository.findByPerfil(perfil)
                .orElseGet(() -> {
                    EstadisticaUsuario nuevaEstadistica = new EstadisticaUsuario();
                    nuevaEstadistica.setPerfil(perfil);
                    return estadisticaRepository.save(nuevaEstadistica);
                });
    }

    private BadgeObtenidoResponse crearRespuestaBadgeObtenido(List<Badge> nuevosBadges) {
        BadgeObtenidoResponse response = new BadgeObtenidoResponse();
        response.setNuevosBadges(nuevosBadges.stream()
                .map(this::convertirABadgeResponse)
                .collect(Collectors.toList()));
        response.setMostrarNotificacion(!nuevosBadges.isEmpty());
        response.setMensaje(nuevosBadges.isEmpty() ? 
                "No hay nuevos badges" : 
                "¡Felicidades! Has obtenido " + nuevosBadges.size() + " nuevo(s) badge(s)");
        response.setFechaObtenido(LocalDateTime.now());
        return response;
    }

    private BadgeResponse convertirABadgeResponse(Badge badge) {
        BadgeResponse response = new BadgeResponse();
        response.setId(badge.getId());
        response.setNombre(badge.getNombre());
        response.setDescripcion(badge.getDescripcion());
        response.setIcono(badge.getIcono());
        response.setColor(badge.getColor());
        response.setTipo(badge.getTipo().getDisplayName());
        response.setCondicionRequerida(badge.getCondicionRequerida());
        return response;
    }

    private UsuarioBadgeResponse convertirAUsuarioBadgeResponse(UsuarioBadge usuarioBadge) {
        UsuarioBadgeResponse response = new UsuarioBadgeResponse();
        response.setId(usuarioBadge.getId());
        response.setBadge(convertirABadgeResponse(usuarioBadge.getBadge()));
        response.setFechaObtenido(usuarioBadge.getFechaObtenido());
        response.setProgresoActual(usuarioBadge.getProgresoActual());
        response.setEsNuevo(!usuarioBadge.getNotificado());
        return response;
    }
}