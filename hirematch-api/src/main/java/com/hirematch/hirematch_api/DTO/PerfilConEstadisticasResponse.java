package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PerfilConEstadisticasResponse {
    // Información del perfil existente
    private Long perfilId;
    private String nombreCompleto;
    private String email;
    private String tipoPerfil;
    private String nombreEmpresa;
    private String descripcion;
    private String ubicacion;
    private String telefono;
    private String sitioWeb;
    private String experiencia;
    private String habilidades;
    private String educacion;
    private String certificaciones;
    private String intereses;
    private String fotoUrl;
    
    // Nuevas estadísticas y badges
    private EstadisticaUsuarioResponse estadisticas;
    private List<UsuarioBadgeResponse> badges;
    private List<UsuarioBadgeResponse> badgesRecientes; // Últimos 7 días
    private List<BadgeResponse> badgesDisponibles; // Badges que puede obtener
    private List<ProgresoResponse> proximosBadges; // Progreso hacia badges
    
    // Gamificación
    private Integer nivelUsuario; // Calculado basado en actividad
    private String titulo; // "Novato", "Experto", "Maestro", etc.
    private Integer puntosExperiencia; // Puntos totales acumulados
    private Integer puntosParaProximoNivel; // Puntos necesarios para siguiente nivel
    private Double progresoNivel; // Progreso hacia el siguiente nivel (0-100)
    
    // Actividad reciente
    private LocalDateTime ultimaConexion;
    private List<ActividadRecienteResponse> actividadReciente;
    
    // Logros destacados
    private List<LogroDestacadoResponse> logrosDestacados;
}