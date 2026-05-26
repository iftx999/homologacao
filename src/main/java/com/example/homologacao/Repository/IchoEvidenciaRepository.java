package com.example.homologacao.Repository;

import com.example.homologacao.model.IchoEvidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IchoEvidenciaRepository extends JpaRepository<IchoEvidencia, Long> {

    List<IchoEvidencia> findByIchoIdOrderByCriadoEmDesc(Long ichoId);

    List<IchoEvidencia> findByIchoIdInOrderByCriadoEmDesc(List<Long> ichoIds);
}
