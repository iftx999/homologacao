package com.example.homologacao.model.Enum;

public enum StatusIcho {
    NAO_TESTADO,
    EM_TESTE,
    PENDENTE,
    FALHA,
    EM_CORRECAO,
    RETESTE,
    OK;

    public boolean isFinalizadoComSucesso() {
        return this == OK;
    }

    public boolean isPendenteHomologacao() {
        return this != OK;
    }
}
