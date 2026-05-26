package com.example.homologacao.Repository;

import com.example.homologacao.model.GrupoUsuarioMembro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface GrupoUsuarioMembroRepository extends JpaRepository<GrupoUsuarioMembro, Long> {

    Optional<GrupoUsuarioMembro> findByGrupoIdAndUsuarioId(Long grupoId, Long usuarioId);

    boolean existsByGrupoIdAndUsuarioId(Long grupoId, Long usuarioId);

    boolean existsByGrupoImplantacaoIdAndUsuarioId(Long implantacaoId, Long usuarioId);

    List<GrupoUsuarioMembro> findByGrupoImplantacaoId(Long implantacaoId);
}
