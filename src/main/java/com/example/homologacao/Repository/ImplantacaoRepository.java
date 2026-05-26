package com.example.homologacao.Repository;

import com.example.homologacao.model.Enum.StatusImplantacao;
import com.example.homologacao.model.Implantacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImplantacaoRepository extends JpaRepository<Implantacao, Long> {


    List<Implantacao> findByStatus(StatusImplantacao status);

    long countByStatus(StatusImplantacao status);

    long countByIdIn(List<Long> ids);

    long countByIdInAndStatus(List<Long> ids, StatusImplantacao status);

    @Query("""
            select i.status, count(i)
            from Implantacao i
            group by i.status
            """)
    List<Object[]> countPorStatus();

    @Query("""
            select i.status, count(i)
            from Implantacao i
            where i.id in :ids
            group by i.status
            """)
    List<Object[]> countPorStatusByIdIn(List<Long> ids);
}
