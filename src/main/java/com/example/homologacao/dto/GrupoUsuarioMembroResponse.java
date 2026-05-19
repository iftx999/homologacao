package com.example.homologacao.dto;

import com.example.homologacao.model.GrupoUsuarioMembro;

public class GrupoUsuarioMembroResponse {

    private Long id;
    private Long usuarioId;
    private String username;
    private String funcao;

    public static GrupoUsuarioMembroResponse fromEntity(GrupoUsuarioMembro membro) {
        GrupoUsuarioMembroResponse response = new GrupoUsuarioMembroResponse();
        response.setId(membro.getId());
        response.setUsuarioId(membro.getUsuario().getId());
        response.setUsername(membro.getUsuario().getUsername());
        response.setFuncao(membro.getFuncao());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFuncao() {
        return funcao;
    }

    public void setFuncao(String funcao) {
        this.funcao = funcao;
    }
}
