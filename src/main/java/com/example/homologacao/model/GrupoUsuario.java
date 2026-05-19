package com.example.homologacao.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "grupo_usuario")
public class GrupoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @ManyToOne
    @JoinColumn(name = "implantacao_id", nullable = false)
    private Implantacao implantacao;

    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GrupoUsuarioMembro> membros = new ArrayList<>();

    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GrupoUsuarioPermissao> permissoes = new ArrayList<>();

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

    public Implantacao getImplantacao() {
        return implantacao;
    }

    public void setImplantacao(Implantacao implantacao) {
        this.implantacao = implantacao;
    }

    public List<GrupoUsuarioMembro> getMembros() {
        return membros;
    }

    public void setMembros(List<GrupoUsuarioMembro> membros) {
        this.membros = membros;
    }

    public List<GrupoUsuarioPermissao> getPermissoes() {
        return permissoes;
    }

    public void setPermissoes(List<GrupoUsuarioPermissao> permissoes) {
        this.permissoes = permissoes;
    }
}
