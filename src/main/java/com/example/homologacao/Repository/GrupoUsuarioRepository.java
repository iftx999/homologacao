package com.example.homologacao.Repository;

import com.example.homologacao.model.GrupoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GrupoUsuarioRepository extends JpaRepository<GrupoUsuario, Long> {

    List<GrupoUsuario> findByImplantacaoId(Long implantacaoId);
}
