package com.example.homologacao.Repository;

import com.example.homologacao.model.Implantacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImplantacaoRepository extends JpaRepository<Implantacao, Long> {


    List<Implantacao> findByStatus(Model.Enum.StatusImplantacao status);
}
