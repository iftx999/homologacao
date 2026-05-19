package com.example.homologacao.dto;

import com.example.homologacao.model.Enum.AcaoPermissao;
import com.example.homologacao.model.Enum.RecursoSistema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public class ConfigurarPermissoesGrupoUsuarioRequest {

    @NotNull
    private RecursoSistema recurso;

    @NotEmpty
    private Set<AcaoPermissao> acoes;

    public RecursoSistema getRecurso() {
        return recurso;
    }

    public void setRecurso(RecursoSistema recurso) {
        this.recurso = recurso;
    }

    public Set<AcaoPermissao> getAcoes() {
        return acoes;
    }

    public void setAcoes(Set<AcaoPermissao> acoes) {
        this.acoes = acoes;
    }
}
