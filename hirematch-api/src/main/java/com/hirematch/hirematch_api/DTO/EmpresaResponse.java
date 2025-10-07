package com.hirematch.hirematch_api.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaResponse {
    private Long empresaId;
    private String nombreEmpresa;
    private String descripcion;
    private String sitioWeb;
}
