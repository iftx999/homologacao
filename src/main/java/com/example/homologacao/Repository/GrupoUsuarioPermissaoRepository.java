package com.example.homologacao.Repository;

import com.example.homologacao.model.Enum.AcaoPermissao;
import com.example.homologacao.model.Enum.RecursoSistema;
import com.example.homologacao.model.GrupoUsuarioPermissao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface GrupoUsuarioPermissaoRepository extends JpaRepository<GrupoUsuarioPermissao, Long> {

    List<GrupoUsuarioPermissao> findByGrupoId(Long grupoId);

    void deleteByGrupoId(Long grupoId);

    @Query("""
            select count(p) > 0
            from GrupoUsuarioPermissao p
            join p.grupo.membros m
            where m.usuario.username = :username
              and p.recurso = :recurso
              and p.acao in :acoes
            """)
    boolean existsPermissaoGeral(
            @Param("username") String username,
            @Param("recurso") RecursoSistema recurso,
            @Param("acoes") Collection<AcaoPermissao> acoes
    );

    @Query("""
            select count(p) > 0
            from GrupoUsuarioPermissao p
            join p.grupo.membros m
            where p.grupo.implantacao.id = :implantacaoId
              and m.usuario.username = :username
              and p.recurso = :recurso
              and p.acao in :acoes
            """)
    boolean existsPermissaoNaImplantacao(
            @Param("implantacaoId") Long implantacaoId,
            @Param("username") String username,
            @Param("recurso") RecursoSistema recurso,
            @Param("acoes") Collection<AcaoPermissao> acoes
    );

    @Query("""
            select distinct p.grupo.implantacao.id
            from GrupoUsuarioPermissao p
            join p.grupo.membros m
            where m.usuario.username = :username
              and p.recurso = :recurso
              and p.acao in :acoes
            """)
    List<Long> findImplantacaoIdsPermitidas(
            @Param("username") String username,
            @Param("recurso") RecursoSistema recurso,
            @Param("acoes") Collection<AcaoPermissao> acoes
    );
}
