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
}
