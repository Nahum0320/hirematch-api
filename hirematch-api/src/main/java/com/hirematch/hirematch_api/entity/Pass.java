package com.hirematch.hirematch_api.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "passes", uniqueConstraints = @UniqueConstraint(columnNames = {"perfil_id", "oferta_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pass_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_id", nullable = false)
    private Perfil perfil; // Perfil del postulante que da pass

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oferta_id", nullable = false)
    private OfertaLaboral oferta; // Oferta laboral que recibe el pass

    @Column(name = "fecha_pass", nullable = false)
    private LocalDateTime fechaPass;

    @PrePersist
    protected void onCreate() {
        fechaPass = LocalDateTime.now();
    }
}