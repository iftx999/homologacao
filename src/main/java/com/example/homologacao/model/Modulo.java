package com.example.homologacao.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "modulo")
public class Modulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String descricao;

    @ManyToOne
    @JoinColumn(name = "id_implantacao", nullable = false)
    private Implantacao implantacao;
}
