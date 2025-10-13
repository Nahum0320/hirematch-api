package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagoSuscripcionResponse {
    private String paymentUrl;
    private String tilopayLinkId;
    private String error;
}