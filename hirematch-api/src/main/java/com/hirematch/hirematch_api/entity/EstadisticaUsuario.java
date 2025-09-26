package com.hirematch.hirematch_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "estadisticas_usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticaUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "estadistica_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_id", nullable = false, unique = true)
    private Perfil perfil;

    // Estadísticas de matches
    @Column(name = "total_matches", nullable = false)
    private Integer totalMatches = 0;

    @Column(name = "total_likes_dados", nullable = false)
    private Integer totalLikesDados = 0;

    @Column(name = "total_likes_recibidos", nullable = false)
    private Integer totalLikesRecibidos = 0;

    @Column(name = "total_superlikes_dados", nullable = false)
    private Integer totalSuperlikesDados = 0;

    @Column(name = "total_superlikes_recibidos", nullable = false)
    private Integer totalSuperlikesRecibidos = 0;

    @Column(name = "total_rechazos_dados", nullable = false)
    private Integer totalRechazosDados = 0;

    @Column(name = "total_rechazos_recibidos", nullable = false)
    private Integer totalRechazosRecibidos = 0;

    // Estadísticas de perfil
    @Column(name = "perfil_completado", nullable = false)
    private Boolean perfilCompletado = false;

    @Column(name = "porcentaje_perfil", nullable = false)
    private Integer porcentajePerfil = 0;

    // Estadísticas de actividad
    @Column(name = "dias_activo", nullable = false)
    private Integer diasActivo = 0;

    @Column(name = "ultima_actividad")
    private LocalDateTime ultimaActividad;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}