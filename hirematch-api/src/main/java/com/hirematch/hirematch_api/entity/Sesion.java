package com.hirematch.hirematch_api.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.*;


@Entity
@Table(name = "sesiones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "numero_sesion")
    private Long numeroSesion;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fechainicio", updatable = false,nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fechafin", nullable = false)
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoSesion estado;

    @PrePersist
    protected void onCreate() {
        fechaInicio = LocalDateTime.now();
        fechaFin = LocalDateTime.now().plusHours(2); // La sesión dura 2 horas por defecto

    }

    public boolean isActiva() {
        return EstadoSesion.ACTIVA.equals(estado);
    }
    
    public boolean isExpirada() {
        return EstadoSesion.EXPIRADA.equals(estado);
    }
    public boolean isCancelada() {
        return EstadoSesion.CANCELADA.equals(estado);
    }
    
    public void activar() {
        this.estado = EstadoSesion.ACTIVA;
    }
    
    public void expirar() {
        this.estado = EstadoSesion.EXPIRADA;
    }

    public void cancelar() {
        this.estado = EstadoSesion.CANCELADA;
    }

     public boolean hasExpired() {
        return LocalDateTime.now().isAfter(fechaFin);
    }
    public void extenderSesion() {
        this.fechaFin = this.fechaFin.plusHours(2);
    }
}
