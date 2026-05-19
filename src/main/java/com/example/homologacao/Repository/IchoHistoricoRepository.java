package com.example.homologacao.Repository;

import com.example.homologacao.model.IchoHistorico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IchoHistoricoRepository extends JpaRepository<IchoHistorico, Long> {

    boolean existsByIchoId(Long ichoId);

    List<IchoHistorico> findByIchoIdOrderByDataAlteracaoAsc(Long ichoId);
}
