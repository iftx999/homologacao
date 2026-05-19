package com.example.homologacao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CriarGrupoUsuarioRequest {

    @NotBlank
    private String nome;

    @NotNull
    private Long implantacaoId;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Long getImplantacaoId() {
        return implantacaoId;
    }

    public void setImplantacaoId(Long implantacaoId) {
        this.implantacaoId = implantacaoId;
    }
}
