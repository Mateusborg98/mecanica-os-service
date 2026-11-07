package br.com.techchallenge.mecanica.presentation.dto.erro;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErroResponse(
        OffsetDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        Map<String, String> campos) {
}
