package com.hirematch.hirematch_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reportes",
        indexes = {
                @Index(name = "idx_reporte_estado", columnList = "estado"),
                @Index(name = "idx_reporte_tipo", columnList = "tipo"),
                @Index(name = "idx_reporte_fecha", columnList = "fecha"),
                @Index(name = "idx_reporte_reportante", columnList = "reportante_id"),
                @Index(name = "idx_reporte_reportado", columnList = "reportado_id"),
                @Index(name = "idx_reporte_oferta", columnList = "oferta_id")
        }
)
@Data
@NoArgsConstructor
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportante_id", nullable = false)
    private Usuario reportante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportado_id")
    private Usuario reportado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oferta_id")
    private OfertaLaboral oferta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoReporte tipo;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    @Column(nullable = false, length = 500)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReporte estado = EstadoReporte.PENDIENTE;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @PrePersist
    protected void onCreate() {
        this.fecha = LocalDateTime.now();
        this.validateReporte();
    }

    @PreUpdate
    protected void onUpdate() {
        this.validateReporte();
    }

    private void validateReporte() {
        if ((reportado == null && oferta == null) || (reportado != null && oferta != null)) {
            throw new IllegalStateException("El reporte debe estar asociado a un usuario o una oferta, pero no a ambos");
        }
    }
}