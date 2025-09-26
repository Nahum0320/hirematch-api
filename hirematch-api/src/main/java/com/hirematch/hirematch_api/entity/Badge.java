package com.hirematch.hirematch_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "badges")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "badge_id")
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "icono", nullable = false, length = 50)
    private String icono; // Nombre del icono MaterialIcons

    @Column(name = "color", nullable = false, length = 7)
    private String color; // Color hex para el badge

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoBadge tipo;

    @Column(name = "condicion_requerida")
    private Integer condicionRequerida; // Cantidad requerida para desbloquear

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }

    public enum TipoBadge {
        PRIMER_MATCH("Primer Match"),
        MATCHES_CANTIDAD("Matches por Cantidad"),
        PRIMER_LIKE("Primer Like"),
        LIKES_CANTIDAD("Likes por Cantidad"),
        PRIMER_SUPERLIKE("Primer SuperLike"),
        SUPERLIKES_CANTIDAD("SuperLikes por Cantidad"),
        PERFIL_COMPLETO("Perfil Completo"),
        ACTIVIDAD_DIARIA("Actividad Diaria"),
        VETERANO("Veterano"),
        POPULARIDAD("Popularidad"),
        ESPECIAL("Especial");

        private final String displayName;

        TipoBadge(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}