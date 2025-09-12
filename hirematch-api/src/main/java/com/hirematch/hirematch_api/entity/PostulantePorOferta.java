package com.hirematch.hirematch_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "postulantes_por_oferta", uniqueConstraints = @UniqueConstraint(columnNames = {"postulante_id", "oferta_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostulantePorOferta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postulante_id", nullable = false)
    private Perfil postulante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oferta_id", nullable = false)
    private OfertaLaboral oferta;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPostulacion estado = EstadoPostulacion.PENDING;

    @Column(name = "super_like", nullable = false)
    private boolean superLike = false;

    @Column(name = "fecha_postulacion", nullable = false)
    private LocalDateTime fechaPostulacion = LocalDateTime.now();

    // Métodos de utilidad
    public void setSuperLike() {
        this.superLike = true;
        this.estado = EstadoPostulacion.SUPERLIKE;
    }

    public void promoverToMatch() {
        this.estado = EstadoPostulacion.MATCHED;
    }

    public void rechazar() {
        this.estado = EstadoPostulacion.REJECTED;
    }

    public void aceptar() {
        this.estado = EstadoPostulacion.ACCEPTED;
    }

    public boolean isPending() {
        return EstadoPostulacion.PENDING.equals(estado);
    }

    public boolean isSuperLike() {
        return superLike;
    }

    public boolean isMatched() {
        return EstadoPostulacion.MATCHED.equals(estado);
    }
}
