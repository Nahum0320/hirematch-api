package com.hirematch.hirematch_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ofertas_guardadas", uniqueConstraints = @UniqueConstraint(columnNames = {"perfil_id", "oferta_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfertaGuardada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_id", nullable = false)
    private Perfil perfil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oferta_id", nullable = false)
    private OfertaLaboral oferta;

    @Column(name = "fecha_guardado", nullable = false)
    private LocalDateTime fechaGuardado = LocalDateTime.now();

    // Convenience method to check if the offer is saved by a specific profile
    public boolean isSavedBy(Perfil perfil) {
        return this.perfil.equals(perfil);
    }

    // Convenience method to check if the offer matches a specific oferta
    public boolean matchesOferta(OfertaLaboral oferta) {
        return this.oferta.equals(oferta);
    }
}