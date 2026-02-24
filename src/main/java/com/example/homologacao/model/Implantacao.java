package com.example.homologacao.model;

import Model.Enum.StatusImplantacao;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "implantacao")
public class Implantacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "data_go_live", nullable = false)
    private LocalDate dataGoLive;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusImplantacao status;

    private String observacao;

    @OneToMany(mappedBy = "implantacao", cascade = CascadeType.ALL)
    private List<Icho> ichos;

    public Implantacao() {
    }
    //teste
    public Implantacao(Long id, String nome, LocalDate dataGoLive, StatusImplantacao status, String observacao, List<Icho> ichos) {
        this.id = id;
        this.nome = nome;
        this.dataGoLive = dataGoLive;
        this.status = status;
        this.observacao = observacao;
        this.ichos = ichos;
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

    public LocalDate getDataGoLive() {
        return dataGoLive;
    }

    public void setDataGoLive(LocalDate dataGoLive) {
        this.dataGoLive = dataGoLive;
    }

    public StatusImplantacao getStatus() {
        return status;
    }

    public void setStatus(StatusImplantacao status) {
        this.status = status;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public List<Icho> getIchos() {
        return ichos;
    }

    public void setIchos(List<Icho> ichos) {
        this.ichos = ichos;
    }
}
