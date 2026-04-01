package com.example.homologacao.Repository;

import com.example.homologacao.model.Enum.StatusIcho;
import com.example.homologacao.model.Icho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IchoRepository extends JpaRepository<Icho, Long> {

    // =========================
    // MODULO
    // =========================

    List<Icho> findByModuloIdAndStatus(Long moduloId, StatusIcho status);

    boolean existsByModuloIdAndStatusIn(Long moduloId, List<StatusIcho> status);

    List<Icho> findByModuloId(Long moduloid);

    // =========================
    // PENDÊNCIAS
    // =========================

    @Query(value = """
    SELECT i.*
    FROM icho i
    INNER JOIN modulo m ON m.id = i.modulo_id
    INNER JOIN implantacao imp ON imp.id = m.implantacao_id
    WHERE imp.data_go_live <= CURRENT_DATE
    AND i.status IN (:statusList)
""", nativeQuery = true)
    List<Icho> buscarPendentesComGoLiveEstourado(@Param("statusList") List<String> statusList);

    @Query("""
    SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END
    FROM Icho i
    WHERE i.modulo = :moduloId
    AND i.status IN (:statusList)
""")
    boolean existsPendencias(
            @Param("moduloId") Long moduloId,
            @Param("statusList") List<StatusIcho> statusList
    );

    @Query("""
        SELECT i FROM Icho i
        WHERE i.status IN (:statusList)
    """)
    List<Icho> buscarPendencias(@Param("statusList") List<StatusIcho> statusList);

    @Query("""
SELECT i FROM Icho i
WHERE i.modulo.id = :moduloId
AND i.status IN (:statusList)
""")
    List<Icho> buscarPendenciasPorModulo(
            @Param("moduloId") Long moduloId,
            @Param("statusList") List<StatusIcho> statusList
    );
}