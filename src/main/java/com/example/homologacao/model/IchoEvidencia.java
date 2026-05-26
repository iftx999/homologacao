package com.example.homologacao.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "icho_evidencia")
@Getter
@Setter
public class IchoEvidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icho_id", nullable = false)
    private Icho icho;

    @Column(nullable = false)
    private String nomeArquivoOriginal;

    @Column(nullable = false)
    private String nomeArquivoArmazenado;

    @Column(nullable = false)
    private String tipoConteudo;

    @Column(nullable = false)
    private Long tamanhoBytes;

    private String descricao;

    private String criadoPor;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    public IchoEvidencia() {
    }
}
