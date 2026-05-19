package com.example.homologacao.dto;

import com.example.homologacao.model.Enum.AcaoPermissao;
import com.example.homologacao.model.Enum.RecursoSistema;
import com.example.homologacao.model.GrupoUsuarioPermissao;

public class GrupoUsuarioPermissaoResponse {

    private Long id;
    private RecursoSistema recurso;
    private AcaoPermissao acao;

    public static GrupoUsuarioPermissaoResponse fromEntity(GrupoUsuarioPermissao permissao) {
        GrupoUsuarioPermissaoResponse response = new GrupoUsuarioPermissaoResponse();
        response.setId(permissao.getId());
        response.setRecurso(permissao.getRecurso());
        response.setAcao(permissao.getAcao());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RecursoSistema getRecurso() {
        return recurso;
    }

    public void setRecurso(RecursoSistema recurso) {
        this.recurso = recurso;
    }

    public AcaoPermissao getAcao() {
        return acao;
    }

    public void setAcao(AcaoPermissao acao) {
        this.acao = acao;
    }
}
