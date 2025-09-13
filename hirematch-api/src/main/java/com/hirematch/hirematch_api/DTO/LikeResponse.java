package com.hirematch.hirematch_api.DTO;

import com.hirematch.hirematch_api.entity.EstadoPostulacion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LikeResponse {
    private Long LikeId; // o Long id;
    private String usuarioEmail; // NECESARIO para el frontend
    private LocalDateTime FechaLike; // NECESARIO para el frontend  
    private String tipoLike; // NECESARIO para el frontend
    
    // Campos adicionales que ya tienes
    private Long perfilId;
    private Long ofertaId;
    private String estado;
    private Boolean superLike;
    
    // getters y setters para todos
}