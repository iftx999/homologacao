package com.example.homologacao.dto;

import com.example.homologacao.model.GrupoUsuario;

import java.util.List;

public class GrupoUsuarioResponse {

    private Long id;
    private String nome;
    private Long implantacaoId;
    private String implantacaoNome;
    private List<GrupoUsuarioMembroResponse> membros;
    private List<GrupoUsuarioPermissaoResponse> permissoes;

    public static GrupoUsuarioResponse fromEntity(GrupoUsuario grupo) {
        GrupoUsuarioResponse response = new GrupoUsuarioResponse();
        response.setId(grupo.getId());
        response.setNome(grupo.getNome());
        response.setImplantacaoId(grupo.getImplantacao().getId());
        response.setImplantacaoNome(grupo.getImplantacao().getNome());
        response.setMembros(grupo.getMembros().stream()
                .map(GrupoUsuarioMembroResponse::fromEntity)
                .toList());
        response.setPermissoes(grupo.getPermissoes().stream()
                .map(GrupoUsuarioPermissaoResponse::fromEntity)
                .toList());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getImplantacaoNome() {
        return implantacaoNome;
    }

    public void setImplantacaoNome(String implantacaoNome) {
        this.implantacaoNome = implantacaoNome;
    }

    public List<GrupoUsuarioMembroResponse> getMembros() {
        return membros;
    }

    public void setMembros(List<GrupoUsuarioMembroResponse> membros) {
        this.membros = membros;
    }

    public List<GrupoUsuarioPermissaoResponse> getPermissoes() {
        return permissoes;
    }

    public void setPermissoes(List<GrupoUsuarioPermissaoResponse> permissoes) {
        this.permissoes = permissoes;
    }
}
