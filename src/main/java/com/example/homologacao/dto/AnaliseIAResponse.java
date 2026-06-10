package com.example.homologacao.dto;

import java.util.List;

public record AnaliseIAResponse(
        String resumoExecutivo,
        NivelRiscoIA nivelRisco,
        List<RiscoIA> riscos,
        List<PendenciaIA> pendencias,
        List<AcaoIA> planoAcao,
        List<String> perguntasSugeridas
) {

    public enum NivelRiscoIA {
        BAIXO,
        MEDIO,
        ALTO
    }

    public enum PrioridadeIA {
        BAIXA,
        MEDIA,
        ALTA
    }

    public record RiscoIA(
            String titulo,
            String descricao,
            PrioridadeIA severidade,
            String modulo,
            String icho
    ) {
    }

    public record PendenciaIA(
            String descricao,
            String modulo,
            String icho,
            PrioridadeIA prioridade
    ) {
    }

    public record AcaoIA(
            String acao,
            String motivo,
            PrioridadeIA prioridade
    ) {
    }
}
