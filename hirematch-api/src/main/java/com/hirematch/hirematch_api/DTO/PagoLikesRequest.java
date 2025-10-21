package com.hirematch.hirematch_api.DTO;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class PagoLikesRequest {
    @NotNull(message = "El ID del producto es requerido")
    private Long productId;

    @NotNull(message = "La URL de callback es requerida")
    private String callbackUrl;
}

