package Model;

import Model.Enum.StatusIcho;
import Model.Implantacao;
import Model.Modulo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "icho")
public class Icho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    private String descricao;

    @ManyToOne
    @JoinColumn(name = "modulo_id", nullable = false)
    private Modulo modulo;

    @ManyToOne
    @JoinColumn(name = "implantacao_id", nullable = false)
    private Implantacao implantacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusIcho status;

    private LocalDate dataTeste;

    private String testadoPor;

    private String observacao;

    public Icho(Long id, String titulo, String descricao, Modulo modulo, Implantacao implantacao, StatusIcho status, LocalDate dataTeste, String testadoPor, String observacao) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.modulo = modulo;
        this.implantacao = implantacao;
        this.status = status;
        this.dataTeste = dataTeste;
        this.testadoPor = testadoPor;
        this.observacao = observacao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Modulo getModulo() {
        return modulo;
    }

    public void setModulo(Modulo modulo) {
        this.modulo = modulo;
    }

    public StatusIcho getStatus() {
        return status;
    }

    public void setStatus(StatusIcho status) {
        this.status = status;
    }

    public Implantacao getImplantacao() {
        return implantacao;
    }

    public void setImplantacao(Implantacao implantacao) {
        this.implantacao = implantacao;
    }

    public LocalDate getDataTeste() {
        return dataTeste;
    }

    public void setDataTeste(LocalDate dataTeste) {
        this.dataTeste = dataTeste;
    }

    public String getTestadoPor() {
        return testadoPor;
    }

    public void setTestadoPor(String testadoPor) {
        this.testadoPor = testadoPor;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
