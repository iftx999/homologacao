package Repository;

import Model.Enum.StatusIcho;
import Model.Icho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IchoRepository extends JpaRepository<Icho, Long> {

    // Buscar ICHOs por implantação
    List<Icho> findByImplantacaoId(Long implantacaoId);

    // Buscar ICHOs por status
    List<Icho> findByStatus(StatusIcho status);

    // ICHOs pendentes ou não testados com go-live vencido
    @Query("""
        SELECT i FROM Icho i
        WHERE i.implantacao.dataGoLive <= CURRENT_DATE
        AND i.status IN ('NAO_TESTADO', 'PENDENTE')
    """)
    List<Icho> buscarPendentesComGoLiveEstourado();

    // ICHOs de um módulo específico
    List<Icho> findByModuloId(Long moduloId);

    // ICHOs por implantação e status
    List<Icho> findByImplantacaoIdAndStatus(Long implantacaoId, StatusIcho status);


    boolean existsByImplantacaoIdAndStatusIn(
            Long implantacaoId,
            List<StatusIcho> status
    );

    // Para scheduler
    boolean existsPendencias(Long implantacaoId);

    // Para tela
    List<Icho> buscarPendencias();

    // Para relatório
    List<Icho> buscarPendenciasPorImplantacao(Long implantacaoId);

}