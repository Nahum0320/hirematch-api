package com.hirematch.hirematch_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ofertas_laborales")
@Data
@NoArgsConstructor
public class OfertaLaboral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "oferta_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    // === INFORMACIÓN BÁSICA ===
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 150, message = "El título no puede exceder 150 caracteres")
    @Column(nullable = false, length = 150)
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres")
    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Size(max = 150, message = "La ubicación no puede exceder 150 caracteres")
    @Column(length = 150)
    private String ubicacion;

    // === DETALLES DEL TRABAJO ===
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_trabajo", length = 50)
    private TipoTrabajo tipoTrabajo; // REMOTO, PRESENCIAL, HIBRIDO

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contrato", length = 50)
    private TipoContrato tipoContrato; // TIEMPO_COMPLETO, MEDIO_TIEMPO, CONTRATO, TEMPORAL, FREELANCE

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_experiencia", length = 50)
    private NivelExperiencia nivelExperiencia; // JUNIOR, SEMI_SENIOR, SENIOR, LEAD, DIRECTOR

    @Size(max = 100, message = "El área no puede exceder 100 caracteres")
    @Column(name = "area_trabajo", length = 100)
    private String areaTrabajo; // Desarrollo, Marketing, Ventas, etc.

    // === COMPENSACIÓN ===
    @DecimalMin(value = "0.0", message = "El salario mínimo debe ser mayor a 0")
    @Column(name = "salario_minimo", precision = 10, scale = 2)
    private BigDecimal salarioMinimo;

    @DecimalMin(value = "0.0", message = "El salario máximo debe ser mayor a 0")
    @Column(name = "salario_maximo", precision = 10, scale = 2)
    private BigDecimal salarioMaximo;

    @Enumerated(EnumType.STRING)
    @Column(name = "moneda", length = 10)
    private Moneda moneda; // USD, CRC, EUR, etc.

    @Column(name = "salario_negociable")
    private Boolean salarioNegociable = false;

    @Column(name = "mostrar_salario")
    private Boolean mostrarSalario = true;

    // === BENEFICIOS Y REQUISITOS ===
    @Size(max = 1500, message = "Los beneficios no pueden exceder 1500 caracteres")
    @Column(name = "beneficios", length = 1500)
    private String beneficios; // Seguro médico, vacaciones, etc.

    @Size(max = 1500, message = "Los requisitos no pueden exceder 1500 caracteres")
    @Column(name = "requisitos", length = 1500)
    private String requisitos; // Experiencia, educación, etc.

    @Size(max = 1000, message = "Las habilidades no pueden exceder 1000 caracteres")
    @Column(name = "habilidades_requeridas", length = 1000)
    private String habilidadesRequeridas; // Java, React, etc.

    @Size(max = 500, message = "Los idiomas no pueden exceder 500 caracteres")
    @Column(name = "idiomas", length = 500)
    private String idiomas; // Español nativo, Inglés avanzado, etc.

    // === FECHAS Y ESTADO ===
    @Column(name = "fecha_publicacion", nullable = false, updatable = false)
    private LocalDateTime fechaPublicacion;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoOferta estado = EstadoOferta.ACTIVA;

    // === CONFIGURACIÓN DE APLICACIÓN ===
    @Column(name = "vacantes_disponibles")
    @Min(value = 1, message = "Debe haber al menos 1 vacante disponible")
    private Integer vacantesDisponibles = 1;

    @Column(name = "aplicacion_rapida")
    private Boolean aplicacionRapida = true; // Permite aplicar con un click

    @Column(name = "preguntas_adicionales", length = 1000)
    private String preguntasAdicionales; // Preguntas personalizadas para candidatos

    // === INFORMACIÓN ADICIONAL PARA UI ===
    @Column(name = "urgente")
    private Boolean urgente = false; // Para destacar en el feed

    @Column(name = "destacada")
    private Boolean destacada = false; // Oferta premium/destacada

    @Size(max = 200, message = "Las etiquetas no pueden exceder 200 caracteres")
    @Column(name = "etiquetas", length = 200)
    private String etiquetas; // Tags separados por comas: "startup,innovador,crecimiento"

    @Column(name = "permite_aplicacion_externa")
    private Boolean permiteAplicacionExterna = false;

    @Size(max = 500, message = "La URL externa no puede exceder 500 caracteres")
    @Column(name = "url_aplicacion_externa", length = 500)
    private String urlAplicacionExterna;

    // === ESTADÍSTICAS ===
    @Column(name = "vistas", columnDefinition = "INTEGER DEFAULT 0")
    private Integer vistas = 0;

    @Column(name = "aplicaciones_recibidas", columnDefinition = "INTEGER DEFAULT 0")
    private Integer aplicacionesRecibidas = 0;

    // === HOOKS DE JPA ===
    @PrePersist
    protected void onCreate() {
        fechaPublicacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (estado == null) {
            estado = EstadoOferta.ACTIVA;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    // === MÉTODOS DE UTILIDAD ===
    public boolean isActiva() {
        return EstadoOferta.ACTIVA.equals(estado);
    }

    public boolean isPausada() {
        return EstadoOferta.PAUSADA.equals(estado);
    }

    public boolean isCerrada() {
        return EstadoOferta.CERRADA.equals(estado);
    }

    public boolean isExpirada() {
        return fechaCierre != null && LocalDateTime.now().isAfter(fechaCierre);
    }

    public String getSalarioFormateado() {
        if (salarioMinimo == null && salarioMaximo == null) {
            return salarioNegociable ? "Salario negociable" : "No especificado";
        }

        String simboloMoneda = moneda != null ? moneda.getSimbolo() : "";

        if (salarioMinimo != null && salarioMaximo != null) {
            return String.format("%s%,.0f - %s%,.0f", simboloMoneda, salarioMinimo, simboloMoneda, salarioMaximo);
        } else if (salarioMinimo != null) {
            return String.format("Desde %s%,.0f", simboloMoneda, salarioMinimo);
        } else {
            return String.format("Hasta %s%,.0f", simboloMoneda, salarioMaximo);
        }
    }

    public List<String> getEtiquetasLista() {
        if (etiquetas == null || etiquetas.trim().isEmpty()) {
            return List.of();
        }
        return List.of(etiquetas.split(",")).stream()
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .toList();
    }

    public void incrementarVistas() {
        this.vistas = (this.vistas == null ? 0 : this.vistas) + 1;
    }

    public void incrementarAplicaciones() {
        this.aplicacionesRecibidas = (this.aplicacionesRecibidas == null ? 0 : this.aplicacionesRecibidas) + 1;
    }
}

// === ENUMS ===

