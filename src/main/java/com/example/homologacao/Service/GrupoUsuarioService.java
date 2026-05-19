package com.example.homologacao.Service;

import com.example.homologacao.Repository.GrupoUsuarioMembroRepository;
import com.example.homologacao.Repository.GrupoUsuarioRepository;
import com.example.homologacao.Repository.ImplantacaoRepository;
import com.example.homologacao.Repository.UserRepository;
import com.example.homologacao.dto.AdicionarMembroGrupoUsuarioRequest;
import com.example.homologacao.dto.ConfigurarPermissoesGrupoUsuarioRequest;
import com.example.homologacao.dto.CriarGrupoUsuarioRequest;
import com.example.homologacao.dto.GrupoUsuarioResponse;
import com.example.homologacao.model.GrupoUsuario;
import com.example.homologacao.model.GrupoUsuarioMembro;
import com.example.homologacao.model.GrupoUsuarioPermissao;
import com.example.homologacao.model.Implantacao;
import com.example.homologacao.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GrupoUsuarioService {

    private final GrupoUsuarioRepository grupoUsuarioRepository;
    private final GrupoUsuarioMembroRepository membroRepository;
    private final ImplantacaoRepository implantacaoRepository;
    private final UserRepository userRepository;

    public GrupoUsuarioService(GrupoUsuarioRepository grupoUsuarioRepository,
                               GrupoUsuarioMembroRepository membroRepository,
                               ImplantacaoRepository implantacaoRepository,
                               UserRepository userRepository) {
        this.grupoUsuarioRepository = grupoUsuarioRepository;
        this.membroRepository = membroRepository;
        this.implantacaoRepository = implantacaoRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public GrupoUsuarioResponse criar(CriarGrupoUsuarioRequest request) {
        Implantacao implantacao = buscarImplantacao(request.getImplantacaoId());

        GrupoUsuario grupo = new GrupoUsuario();
        grupo.setNome(request.getNome());
        grupo.setImplantacao(implantacao);

        return GrupoUsuarioResponse.fromEntity(grupoUsuarioRepository.save(grupo));
    }

    @Transactional(readOnly = true)
    public GrupoUsuarioResponse buscarPorId(Long id) {
        return GrupoUsuarioResponse.fromEntity(buscarGrupo(id));
    }

    @Transactional(readOnly = true)
    public List<GrupoUsuarioResponse> listar(Long implantacaoId) {
        List<GrupoUsuario> grupos = implantacaoId == null
                ? grupoUsuarioRepository.findAll()
                : grupoUsuarioRepository.findByImplantacaoId(implantacaoId);

        return grupos.stream()
                .map(GrupoUsuarioResponse::fromEntity)
                .toList();
    }

    @Transactional
    public GrupoUsuarioResponse adicionarMembro(Long grupoId, AdicionarMembroGrupoUsuarioRequest request) {
        GrupoUsuario grupo = buscarGrupo(grupoId);
        Usuario usuario = buscarUsuario(request.getUsuarioId());

        if (membroRepository.existsByGrupoIdAndUsuarioId(grupoId, request.getUsuarioId())) {
            throw new RuntimeException("Usuário já faz parte deste grupo");
        }

        GrupoUsuarioMembro membro = new GrupoUsuarioMembro();
        membro.setGrupo(grupo);
        membro.setUsuario(usuario);
        membro.setFuncao(request.getFuncao());

        grupo.getMembros().add(membro);
        grupoUsuarioRepository.save(grupo);

        return GrupoUsuarioResponse.fromEntity(grupo);
    }

    @Transactional
    public GrupoUsuarioResponse atualizarFuncao(Long grupoId, Long usuarioId, String funcao) {
        GrupoUsuarioMembro membro = membroRepository.findByGrupoIdAndUsuarioId(grupoId, usuarioId)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado neste grupo"));

        membro.setFuncao(funcao);
        membroRepository.save(membro);

        return GrupoUsuarioResponse.fromEntity(buscarGrupo(grupoId));
    }

    @Transactional
    public void removerMembro(Long grupoId, Long usuarioId) {
        GrupoUsuario grupo = buscarGrupo(grupoId);
        GrupoUsuarioMembro membro = membroRepository.findByGrupoIdAndUsuarioId(grupoId, usuarioId)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado neste grupo"));

        grupo.getMembros().remove(membro);
        grupoUsuarioRepository.save(grupo);
    }

    @Transactional
    public GrupoUsuarioResponse configurarPermissoes(Long grupoId, ConfigurarPermissoesGrupoUsuarioRequest request) {
        GrupoUsuario grupo = buscarGrupo(grupoId);

        grupo.getPermissoes().removeIf(permissao -> permissao.getRecurso() == request.getRecurso());

        request.getAcoes().forEach(acao -> {
            GrupoUsuarioPermissao permissao = new GrupoUsuarioPermissao();
            permissao.setGrupo(grupo);
            permissao.setRecurso(request.getRecurso());
            permissao.setAcao(acao);
            grupo.getPermissoes().add(permissao);
        });

        return GrupoUsuarioResponse.fromEntity(grupoUsuarioRepository.save(grupo));
    }

    @Transactional
    public void removerGrupo(Long id) {
        GrupoUsuario grupo = buscarGrupo(id);
        grupoUsuarioRepository.delete(grupo);
    }

    private GrupoUsuario buscarGrupo(Long id) {
        return grupoUsuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grupo de usuário não encontrado"));
    }

    private Implantacao buscarImplantacao(Long id) {
        return implantacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Implantação não encontrada"));
    }

    private Usuario buscarUsuario(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}
