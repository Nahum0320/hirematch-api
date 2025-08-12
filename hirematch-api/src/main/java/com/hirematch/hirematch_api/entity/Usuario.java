package com.hirematch.hirematch_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre debe tener máximo 150 caracteres")
    @Column(nullable = false, length = 150)
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Size(max = 150, message = "El email debe tener máximo 150 caracteres")
    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @Column(name = "codigo_verificacion", length = 100)
    private String codigoVerificacion;

    @Column(name = "fecha_expiracion_codigo")
    private LocalDateTime fechaExpiracionCodigo;

    @Column(name = "email_verificado")
    private Boolean emailVerificado = false;

    @Column(name = "llave_unica", length = 255)
    private String llaveUnica;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
    }
}