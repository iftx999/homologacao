package com.example.homologacao.model;

import com.example.homologacao.model.Enum.AcaoPermissao;
import com.example.homologacao.model.Enum.RecursoSistema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "grupo_usuario_permissao",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_grupo_usuario_permissao",
                        columnNames = {"grupo_usuario_id", "recurso", "acao"}
                )
        }
)
public class GrupoUsuarioPermissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_usuario_id", nullable = false)
    private GrupoUsuario grupo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecursoSistema recurso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AcaoPermissao acao;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GrupoUsuario getGrupo() {
        return grupo;
    }

    public void setGrupo(GrupoUsuario grupo) {
        this.grupo = grupo;
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
