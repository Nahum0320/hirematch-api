package com.hirematch.hirematch_api.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LikeResponse {
    private Long id;
    private Long perfilId;
    private Long ofertaId;
    private LocalDateTime fechaLike;

}