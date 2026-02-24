package com.example.homologacao.Repository;

import com.example.homologacao.model.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuloRepository extends JpaRepository<Modulo, Long> {

    List<Modulo> findByImplantacaoId(Long id);
}
