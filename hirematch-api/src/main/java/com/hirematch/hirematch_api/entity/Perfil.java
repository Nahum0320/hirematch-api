package com.hirematch.hirematch_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "perfiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "perfil_id")
    private Long id;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotBlank(message = "El tipo de perfil es obligatorio")
    @Pattern(regexp = "postulante|empresa", message = "El tipo de perfil debe ser 'postulante' o 'empresa'")
    @Column(name = "tipo_perfil", length = 50, nullable = false)
    private String tipoPerfil;

    @Size(max = 255, message = "La descripción no debe superar los 255 caracteres")
    private String descripcion;

    @Size(max = 150, message = "La ubicación no debe superar los 150 caracteres")
    private String ubicacion;

    @Size(max = 255, message = "Las habilidades no deben superar los 255 caracteres")
    private String habilidades;
}
