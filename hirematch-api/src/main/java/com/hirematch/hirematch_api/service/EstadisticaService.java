package com.hirematch.hirematch_api.service;

import com.hirematch.hirematch_api.DTO.EstadisticaUsuarioResponse;
import com.hirematch.hirematch_api.entity.EstadisticaUsuario;
import com.hirematch.hirematch_api.entity.Perfil;
import com.hirematch.hirematch_api.repository.EstadisticaUsuarioRepository;
import com.hirematch.hirematch_api.repository.UsuarioBadgeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EstadisticaService {

    @Autowired
    private EstadisticaUsuarioRepository estadisticaRepository;

    @Autowired
    private UsuarioBadgeRepository usuarioBadgeRepository;

    public EstadisticaUsuarioResponse obtenerEstadisticasUsuario(Perfil perfil) {
        EstadisticaUsuario estadisticas = estadisticaRepository.findByPerfil(perfil)
                .orElseGet(() -> crearEstadisticasIniciales(perfil));

        return convertirAEstadisticaResponse(estadisticas);
    }

    @Transactional
    public void actualizarEstadisticaMatch(Perfil perfil) {
        EstadisticaUsuario estadisticas = obtenerOCrearEstadisticas(perfil);
        estadisticas.setTotalMatches(estadisticas.getTotalMatches() + 1);
        estadisticas.setUltimaActividad(LocalDateTime.now());
        estadisticaRepository.save(estadisticas);
    }

    @Transactional
    public void actualizarEstadisticaLikeDado(Perfil perfil) {
        EstadisticaUsuario estadisticas = obtenerOCrearEstadisticas(perfil);
        estadisticas.setTotalLikesDados(estadisticas.getTotalLikesDados() + 1);
        estadisticas.setUltimaActividad(LocalDateTime.now());
        estadisticaRepository.save(estadisticas);
    }

    @Transactional
    public void actualizarEstadisticaLikeRecibido(Perfil perfil) {
        EstadisticaUsuario estadisticas = obtenerOCrearEstadisticas(perfil);
        estadisticas.setTotalLikesRecibidos(estadisticas.getTotalLikesRecibidos() + 1);
        estadisticas.setUltimaActividad(LocalDateTime.now());
        estadisticaRepository.save(estadisticas);
    }

    @Transactional
    public void actualizarEstadisticaSuperlikeDado(Perfil perfil) {
        EstadisticaUsuario estadisticas = obtenerOCrearEstadisticas(perfil);
        estadisticas.setTotalSuperlikesDados(estadisticas.getTotalSuperlikesDados() + 1);
        estadisticas.setUltimaActividad(LocalDateTime.now());
        estadisticaRepository.save(estadisticas);
    }

    @Transactional
    public void actualizarEstadisticaSuperlikeRecibido(Perfil perfil) {
        EstadisticaUsuario estadisticas = obtenerOCrearEstadisticas(perfil);
        estadisticas.setTotalSuperlikesRecibidos(estadisticas.getTotalSuperlikesRecibidos() + 1);
        estadisticas.setUltimaActividad(LocalDateTime.now());
        estadisticaRepository.save(estadisticas);
    }

    @Transactional
    public void actualizarEstadisticaRechazo(Perfil perfilQueDaRechazo, Perfil perfilQueRecibeRechazo) {
        // Actualizar quien da el rechazo
        EstadisticaUsuario estadisticasQueDa = obtenerOCrearEstadisticas(perfilQueDaRechazo);
        estadisticasQueDa.setTotalRechazosDados(estadisticasQueDa.getTotalRechazosDados() + 1);
        estadisticasQueDa.setUltimaActividad(LocalDateTime.now());
        estadisticaRepository.save(estadisticasQueDa);

        // Actualizar quien recibe el rechazo
        EstadisticaUsuario estadisticasQueRecibe = obtenerOCrearEstadisticas(perfilQueRecibeRechazo);
        estadisticasQueRecibe.setTotalRechazosRecibidos(estadisticasQueRecibe.getTotalRechazosRecibidos() + 1);
        estadisticaRepository.save(estadisticasQueRecibe);
    }

    @Transactional
    public void actualizarPorcentajePerfil(Perfil perfil) {
        EstadisticaUsuario estadisticas = obtenerOCrearEstadisticas(perfil);
        Integer porcentaje = calcularPorcentajePerfil(perfil);
        estadisticas.setPorcentajePerfil(porcentaje);
        estadisticas.setPerfilCompletado(porcentaje >= 80);
        estadisticas.setUltimaActividad(LocalDateTime.now());
        estadisticaRepository.save(estadisticas);
    }

    private Integer calcularPorcentajePerfil(Perfil perfil) {
        int campos = 0;
        int camposCompletos = 0;

        // Campos básicos (siempre presentes)
        campos += 2; // email, tipoPerfil
        camposCompletos += 2;

        // Campos opcionales pero importantes
        if (perfil.getDescripcion() != null && !perfil.getDescripcion().trim().isEmpty()) {
            camposCompletos++;
        }
        campos++;

        if (perfil.getUbicacion() != null && !perfil.getUbicacion().trim().isEmpty()) {
            camposCompletos++;
        }
        campos++;

        if (perfil.getTelefono() != null && !perfil.getTelefono().trim().isEmpty()) {
            camposCompletos++;
        }
        campos++;

        if (perfil.getHabilidades() != null && !perfil.getHabilidades().trim().isEmpty()) {
            camposCompletos++;
        }
        campos++;

        // Campos específicos según tipo de perfil
        if ("postulante".equals(perfil.getTipoPerfil())) {
            if (perfil.getExperiencia() != null && !perfil.getExperiencia().trim().isEmpty()) {
                camposCompletos++;
            }
            campos++;

            if (perfil.getEducacion() != null && !perfil.getEducacion().trim().isEmpty()) {
                camposCompletos++;
            }
            campos++;
        } else if ("empresa".equals(perfil.getTipoPerfil())) {

            if (perfil.getSitioWeb() != null && !perfil.getSitioWeb().trim().isEmpty()) {
                camposCompletos++;
            }
            campos++;
        }

        return (int) Math.round((double) camposCompletos / campos * 100);
    }

    private EstadisticaUsuario obtenerOCrearEstadisticas(Perfil perfil) {
        return estadisticaRepository.findByPerfil(perfil)
                .orElseGet(() -> crearEstadisticasIniciales(perfil));
    }

    private EstadisticaUsuario crearEstadisticasIniciales(Perfil perfil) {
        EstadisticaUsuario estadisticas = new EstadisticaUsuario();
        estadisticas.setPerfil(perfil);
        estadisticas.setFechaRegistro(LocalDateTime.now());
        estadisticas.setUltimaActividad(LocalDateTime.now());
        return estadisticaRepository.save(estadisticas);
    }

    private EstadisticaUsuarioResponse convertirAEstadisticaResponse(EstadisticaUsuario estadisticas) {
        EstadisticaUsuarioResponse response = new EstadisticaUsuarioResponse();
        response.setPerfilId(estadisticas.getPerfil().getPerfilId());
        response.setTotalLikesDados(estadisticas.getTotalLikesDados());
        response.setTotalLikesRecibidos(estadisticas.getTotalLikesRecibidos());
        response.setTotalSuperlikesDados(estadisticas.getTotalSuperlikesDados());
        response.setTotalSuperlikesRecibidos(estadisticas.getTotalSuperlikesRecibidos());
        response.setTotalRechazosDados(estadisticas.getTotalRechazosDados());
        response.setTotalRechazosRecibidos(estadisticas.getTotalRechazosRecibidos());
        response.setPerfilCompletado(estadisticas.getPerfilCompletado());
        response.setPorcentajePerfil(estadisticas.getPorcentajePerfil());
        response.setDiasActivo(estadisticas.getDiasActivo());
        response.setUltimaActividad(estadisticas.getUltimaActividad());
        response.setFechaRegistro(estadisticas.getFechaRegistro());

        // Calcular estadísticas derivadas
        if (estadisticas.getTotalLikesDados() > 0) {
            response.setTasaExito((double) estadisticas.getTotalMatches() / estadisticas.getTotalLikesDados());
        } else {
            response.setTasaExito(0.0);
        }

        if (estadisticas.getTotalLikesDados() > 0) {
            response.setPopularidad((double) estadisticas.getTotalLikesRecibidos() / estadisticas.getTotalLikesDados());
        } else {
            response.setPopularidad(0.0);
        }

        // Contar badges
        Long totalBadges = usuarioBadgeRepository.countBadgesByPerfil(estadisticas.getPerfil());
        response.setTotalBadges(totalBadges.intValue());

        return response;
    }

    public Integer calcularNivelUsuario(Perfil perfil) {
        EstadisticaUsuario estadisticas = estadisticaRepository.findByPerfil(perfil)
                .orElse(new EstadisticaUsuario());

        // Algoritmo simple de nivel basado en actividad
        int puntos = 0;
        puntos += estadisticas.getTotalMatches() * 10;
        puntos += estadisticas.getTotalLikesDados() * 2;
        puntos += estadisticas.getTotalSuperlikesDados() * 5;
        puntos += estadisticas.getPerfilCompletado() ? 50 : 0;
        puntos += usuarioBadgeRepository.countBadgesByPerfil(perfil).intValue() * 20;

        // Convertir puntos a nivel (cada 100 puntos = 1 nivel)
        return Math.max(1, puntos / 100);
    }

    public String obtenerTituloUsuario(Integer nivel) {
        if (nivel >= 20) return "Maestro del Match";
        if (nivel >= 15) return "Experto en Conexiones";
        if (nivel >= 10) return "Profesional Activo";
        if (nivel >= 5) return "Usuario Experimentado";
        if (nivel >= 2) return "Explorador";
        return "Novato";
    }
}