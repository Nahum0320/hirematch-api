package com.hirematch.hirematch_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes", indexes = {
        @Index(name = "idx_mensaje_chat_id", columnList = "chat_id"),
        @Index(name = "idx_mensaje_fecha_envio", columnList = "fecha_envio")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mensaje_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remitente_id", nullable = false)
    private Perfil remitente;

    @NotBlank(message = "El contenido del mensaje no puede estar vacío")
    @Size(max = 1000, message = "El contenido del mensaje no puede exceder 1000 caracteres")
    @Column(name = "contenido", nullable = false)
    private String contenido;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        fechaEnvio = LocalDateTime.now();
    }
}