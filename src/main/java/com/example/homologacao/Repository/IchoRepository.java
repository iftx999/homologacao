package com.example.homologacao.Repository;

import com.example.homologacao.model.Enum.StatusIcho;
import com.example.homologacao.model.Icho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    @Query("""
    SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END
    FROM Icho i
    WHERE i.implantacao.id = :implantacaoId
    AND i.status IN ('PENDENTE', 'NAO_TESTADO')
""")
    boolean existsPendencias(@Param("implantacaoId") Long implantacaoId);

    // Para tela
    @Query("""
    SELECT i FROM Icho i
    WHERE i.status IN ('PENDENTE', 'NAO_TESTADO')
""")
    List<Icho> buscarPendencias();

    // Para relatório
    @Query("""
    SELECT i FROM Icho i
    WHERE i.implantacao.id = :implantacaoId
    AND i.status IN ('PENDENTE', 'NAO_TESTADO')
""")
    List<Icho> buscarPendenciasPorImplantacao(@Param("implantacaoId") Long implantacaoId);

}