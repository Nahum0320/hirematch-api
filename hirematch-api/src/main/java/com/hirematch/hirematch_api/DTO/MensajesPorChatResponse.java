package com.hirematch.hirematch_api.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MensajesPorChatResponse {
    private Long chatId;
    private List<MensajeResponse> mensajes;
}
