package com.example.homologacao.dto;

import com.example.homologacao.model.IchoEvidencia;

import java.time.LocalDateTime;

public record IchoEvidenciaResponse(
        Long id,
        Long ichoId,
        String nomeArquivo,
        String tipoConteudo,
        Long tamanhoBytes,
        String descricao,
        String criadoPor,
        LocalDateTime criadoEm,
        String urlDownload
) {

    public static IchoEvidenciaResponse fromEntity(IchoEvidencia evidencia) {
        Long ichoId = evidencia.getIcho() == null ? null : evidencia.getIcho().getId();
        String urlDownload = evidencia.getId() == null ? null : "/api/ichos/evidencias/" + evidencia.getId() + "/arquivo";
        return new IchoEvidenciaResponse(
                evidencia.getId(),
                ichoId,
                evidencia.getNomeArquivoOriginal(),
                evidencia.getTipoConteudo(),
                evidencia.getTamanhoBytes(),
                evidencia.getDescricao(),
                evidencia.getCriadoPor(),
                evidencia.getCriadoEm(),
                urlDownload
        );
    }
}
