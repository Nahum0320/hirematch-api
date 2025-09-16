package com.hirematch.hirematch_api.DTO;

import com.hirematch.hirematch_api.entity.EstadoPostulacion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PassResponse {
    private Long PassId; // o Long id;
    private String usuarioEmail; // NECESARIO para el frontend
    private LocalDateTime FechaPass; // NECESARIO para el frontend  
    private String tipoPass; // NECESARIO para el frontend
    
    // Campos adicionales que ya tienes
    private Long perfilId;
    private Long ofertaId;
    private String estado;
    private Boolean superPass;
    
    // getters y setters para todos
}