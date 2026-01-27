package Model;

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
}
