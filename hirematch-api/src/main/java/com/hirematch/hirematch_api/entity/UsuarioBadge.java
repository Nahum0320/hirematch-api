package com.hirematch.hirematch_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_badges", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"perfil_id", "badge_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_badge_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_id", nullable = false)
    private Perfil perfil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @Column(name = "fecha_obtenido", nullable = false)
    private LocalDateTime fechaObtenido = LocalDateTime.now();

    @Column(name = "progreso_actual")
    private Integer progresoActual; // Para badges que requieren progreso

    @Column(name = "notificado", nullable = false)
    private Boolean notificado = false; // Para saber si se ha notificado al usuario

    @PrePersist
    protected void onCreate() {
        fechaObtenido = LocalDateTime.now();
    }
}