package com.hirematch.hirematch_api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "chats")
@NoArgsConstructor
@AllArgsConstructor
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private Long id;

    @Column (name = "chat_uid", nullable =false, unique = true, length = 255)
    private String chatUid;

    @OneToOne
    @JoinColumn(name = "match_id", nullable = false, unique = true)
    private Match match;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    // TODO: cambiar a clase mensaje cuando hayan
    private List<Object> mensajes;

    @Column (name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column (name = "fecha_ultima_interaccion", nullable = false)
    private LocalDateTime fechaUltimaInteraccion;

    @Column (name = "estado", nullable = false)
    private EstadoChat estado;

    @Column (name = "fecha_archivamiento", nullable = true)
    private LocalDateTime fechaArchivamiento;

    @Column(name = "no_leidos_postulante", columnDefinition = "INT DEFAULT 0")
    private int noLeidosPostulante = 0;

    @Column(name = "no_leidos_empresa", columnDefinition = "INT DEFAULT 0")
    private int noLeidosEmpresa = 0;


    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        fechaUltimaInteraccion = now;
        fechaUltimaInteraccion = now; // Al crearse, la última actividad es la creación
        estado = EstadoChat.ACTIVO; // Los chats nacen activos
        if (this.chatUid == null) {
            this.chatUid = UUID.randomUUID().toString();
        }
    }

}
