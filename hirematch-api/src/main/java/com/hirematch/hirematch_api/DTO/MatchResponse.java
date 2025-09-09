package com.hirematch.hirematch_api.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchResponse {

    private Long id;
    private Long likeId;
    private Long empresaId;
    private LocalDateTime fechaMatch;

    // Getters and Setters
}
