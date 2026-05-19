package com.example.homologacao.model;

import com.example.homologacao.model.Enum.StatusIcho;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "icho_historico")
@Getter
@Setter
public class IchoHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "icho_id")
    private Icho icho;

    @Enumerated(EnumType.STRING)
    private StatusIcho statusAnterior;

    @Enumerated(EnumType.STRING)
    private StatusIcho novoStatus;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "usuario_nome")
    private String usuarioNome;

    @Column(name = "data_alteracao", nullable = false)
    private LocalDateTime dataAlteracao;

    public IchoHistorico() {
    }
}
