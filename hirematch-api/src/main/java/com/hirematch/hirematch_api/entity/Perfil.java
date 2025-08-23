package com.hirematch.hirematch_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "perfiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "perfil_id")
    private Long perfilId;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "tipo_perfil", nullable = false, length = 50)
    private String tipoPerfil;

    private String descripcion;
    private String ubicacion;
    private String habilidades;
    private String telefono;
    private String sitioWeb;

    @Column(columnDefinition = "text")
    private String experiencia;

    @Column(columnDefinition = "text")
    private String educacion;

    @Column(columnDefinition = "text")
    private String certificaciones;

    @Column(columnDefinition = "text")
    private String intereses;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
