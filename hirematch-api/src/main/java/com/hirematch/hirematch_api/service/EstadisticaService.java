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
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class EstadisticaService {

    @Autowired
    private EstadisticaUsuarioRepository estadisticaRepository;

    @Autowired
    private UsuarioBadgeRepository usuarioBadgeRepository;

    @Autowired
    private BadgeService badgeService;

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
        badgeService.verificarYOtorgarBadges(perfil);
    }

    @Transactional
    public void actualizarEstadisticaLikeDado(Perfil perfil) {
        EstadisticaUsuario estadisticas = obtenerOCrearEstadisticas(perfil);
        estadisticas.setTotalLikesDados(estadisticas.getTotalLikesDados() + 1);
        estadisticas.setUltimaActividad(LocalDateTime.now());
        estadisticaRepository.save(estadisticas);
        badgeService.verificarYOtorgarBadges(perfil);
    }

    @Transactional
    public void actualizarEstadisticaLikeRecibido(Perfil perfil) {
        EstadisticaUsuario estadisticas = obtenerOCrearEstadisticas(perfil);
        estadisticas.setTotalLikesRecibidos(estadisticas.getTotalLikesRecibidos() + 1);
        estadisticas.setUltimaActividad(LocalDateTime.now());
        estadisticaRepository.save(estadisticas);
        badgeService.verificarYOtorgarBadges(perfil);
    }

    @Transactional
    public void actualizarEstadisticaSuperlikeDado(Perfil perfil) {
        EstadisticaUsuario estadisticas = obtenerOCrearEstadisticas(perfil);
        estadisticas.setTotalSuperlikesDados(estadisticas.getTotalSuperlikesDados() + 1);
        estadisticas.setUltimaActividad(LocalDateTime.now());
        estadisticaRepository.save(estadisticas);
        badgeService.verificarYOtorgarBadges(perfil);
    }

    @Transactional
    public void actualizarEstadisticaSuperlikeRecibido(Perfil perfil) {
        EstadisticaUsuario estadisticas = obtenerOCrearEstadisticas(perfil);
        estadisticas.setTotalSuperlikesRecibidos(estadisticas.getTotalSuperlikesRecibidos() + 1);
        estadisticas.setUltimaActividad(LocalDateTime.now());
        estadisticaRepository.save(estadisticas);
        badgeService.verificarYOtorgarBadges(perfil);
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
        badgeService.verificarYOtorgarBadges(perfilQueDaRechazo);
        badgeService.verificarYOtorgarBadges(perfilQueRecibeRechazo);
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
        response.setTotalMatches(estadisticas.getTotalMatches());
        response.setTotalLikesDados(estadisticas.getTotalLikesDados());
        response.setTotalLikesRecibidos(estadisticas.getTotalLikesRecibidos());
        response.setTotalSuperlikesDados(estadisticas.getTotalSuperlikesDados());
        response.setTotalSuperlikesRecibidos(estadisticas.getTotalSuperlikesRecibidos());
        response.setTotalRechazosDados(estadisticas.getTotalRechazosDados());
        response.setTotalRechazosRecibidos(estadisticas.getTotalRechazosRecibidos());
        response.setPerfilCompletado(estadisticas.getPerfilCompletado());
        response.setPorcentajePerfil(estadisticas.getPorcentajePerfil());
        response.setDiasActivo((int) ChronoUnit.DAYS.between(estadisticas.getFechaRegistro(), LocalDateTime.now()));
        response.setUltimaActividad(estadisticas.getUltimaActividad());
        response.setFechaRegistro(estadisticas.getFechaRegistro());
        response.setFechaActualizacion(estadisticas.getFechaActualizacion());

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

        int totalLikesDados = estadisticas.getTotalLikesDados() + estadisticas.getTotalSuperlikesDados();
        int totalRespuestas = estadisticas.getTotalLikesRecibidos() + estadisticas.getTotalSuperlikesRecibidos() + estadisticas.getTotalMatches();
        response.setTasaRespuesta(totalLikesDados > 0 ? (double) totalRespuestas / totalLikesDados : 0.0);

        // Calcular rendimiento vs promedio
        Double promedioMatches = estadisticaRepository.getPromedioMatches();
        Double promedioLikesDados = estadisticaRepository.getPromedioLikesDados();
        double tasaExitoPromedio = promedioMatches != null && promedioLikesDados != null && promedioLikesDados > 0
                ? promedioMatches / promedioLikesDados
                : 0.0;
        double tasaExitoUsuario = response.getTasaExito();
        if (tasaExitoUsuario > tasaExitoPromedio * 1.2) {
            response.setRendimientoVsPromedio("Por encima del promedio");
        } else if (tasaExitoUsuario < tasaExitoPromedio * 0.8) {
            response.setRendimientoVsPromedio("Por debajo del promedio");
        } else {
            response.setRendimientoVsPromedio("En el promedio");
        }

        // Calcular posición en ranking
        List<EstadisticaUsuario> topMatches = estadisticaRepository.findTopByMatches();
        long totalUsuarios = estadisticaRepository.count();
        int posicion = topMatches.indexOf(estadisticas) + 1;
        response.setPosicionRanking(posicion > 0 && totalUsuarios > 0 ? (1.0 - (double) posicion / totalUsuarios) * 100 : 0.0);

        // Contar badges
        Long totalBadges = usuarioBadgeRepository.countBadgesByPerfil(estadisticas.getPerfil());
        response.setTotalBadges(totalBadges.intValue());

        return response;
    }

    public EstadisticaUsuarioRepository getEstadisticaUsuarioRepository() {
        return estadisticaRepository;
    }

    public Integer calcularNivelUsuario(Perfil perfil) {
        EstadisticaUsuario estadisticas = estadisticaRepository.findByPerfil(perfil)
                .orElse(new EstadisticaUsuario());

        // Algoritmo simple de nivel basado en actividad
        int puntos = 0;
        puntos += estadisticas.getTotalMatches() * 10;
        puntos += estadisticas.getTotalLikesDados() * 2;
        puntos += estadisticas.getTotalSuperlikesDados() * 5;
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